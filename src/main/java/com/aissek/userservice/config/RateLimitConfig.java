package com.aissek.userservice.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiting Filter to prevent brute-force attacks on authentication endpoints.
 * Limits requests per IP address.
 * 
 * Production Note: For distributed systems, use Redis-based rate limiting instead.
 */
@Slf4j
@Configuration
public class RateLimitConfig {

    private static final long MAX_REQUESTS_PER_MINUTE = 10;
    private static final long RATE_LIMIT_WINDOW_SECONDS = 60;

    @Bean
    public Filter rateLimitingFilter() {
        return new RateLimitingFilter();
    }

    /**
     * Simple in-memory rate limiter using Guava Cache.
     * For production with multiple instances, replace with Redis-based solution.
     */
    private static class RateLimitingFilter implements Filter {
        
        private final LoadingCache<String, AtomicInteger> requestCountsPerIp;

        public RateLimitingFilter() {
            requestCountsPerIp = CacheBuilder.newBuilder()
                    .expireAfterWrite(RATE_LIMIT_WINDOW_SECONDS, TimeUnit.SECONDS)
                    .build(new CacheLoader<String, AtomicInteger>() {
                        @Override
                        public AtomicInteger load(String key) {
                            return new AtomicInteger(0);
                        }
                    });
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // Only apply rate limiting to authentication endpoints
            String requestURI = httpRequest.getRequestURI();
            if (!requestURI.contains("/api/v1/auth")) {
                chain.doFilter(request, response);
                return;
            }

            String clientIp = getClientIp(httpRequest);
            int requests = requestCountsPerIp.getUnchecked(clientIp).incrementAndGet();

            if (requests > MAX_REQUESTS_PER_MINUTE) {
                log.warn("Rate limit exceeded for IP: {}. Blocked request to: {}", clientIp, requestURI);
                
                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpResponse.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                httpResponse.getWriter().write(String.format(
                    "{\"type\":\"about:blank\",\"title\":\"Too Many Requests\",\"status\":429," +
                    "\"detail\":\"Rate limit exceeded. Maximum %d requests per %d seconds.\",\"retryAfter\":%d}",
                    MAX_REQUESTS_PER_MINUTE, RATE_LIMIT_WINDOW_SECONDS, RATE_LIMIT_WINDOW_SECONDS
                ));
                return;
            }

            log.debug("Rate limit check passed for IP: {} ({} requests)", clientIp, requests);
            chain.doFilter(request, response);
        }

        /**
         * Resolve the client IP from the servlet container.
         *
         * We deliberately do NOT read X-Forwarded-For / X-Real-IP directly here:
         * those headers are attacker-controlled and would let a client trivially
         * bypass rate limiting by rotating spoofed values. When deployed behind a
         * trusted proxy, configure {@code server.forward-headers-strategy} (already
         * set to "framework" in the prod profile) so the container populates
         * {@code getRemoteAddr()} from the validated forwarded headers.
         */
        private String getClientIp(HttpServletRequest request) {
            return request.getRemoteAddr();
        }

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            // No initialization needed
        }

        @Override
        public void destroy() {
            requestCountsPerIp.invalidateAll();
        }
    }
}
