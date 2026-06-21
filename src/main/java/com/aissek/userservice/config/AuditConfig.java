package com.aissek.userservice.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Audit Logging Configuration for security-relevant events.
 * Logs authentication attempts, password changes, and token operations.
 * 
 * Production Note: In production, consider sending audit events to:
 * - SIEM system (Splunk, ELK, etc.)
 * - Dedicated audit database
 * - Cloud audit logging service
 */
@Slf4j
@Configuration
public class AuditConfig {

    /**
     * Audit event types for security tracking.
     */
    public enum AuditEventType {
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        LOGOUT,
        PASSWORD_CHANGE_SUCCESS,
        PASSWORD_CHANGE_FAILURE,
        TOKEN_REFRESH_SUCCESS,
        TOKEN_REFRESH_FAILURE,
        USER_CREATED,
        USER_DELETED,
        ACCOUNT_LOCKED
    }

    @Component
    public static class AuditLogger {

        /**
         * Logs security audit events asynchronously on the dedicated audit executor.
         * Includes: timestamp, event type, user identifier, IP address, user agent.
         */
        @Async("auditExecutor")
        public void logAuditEvent(AuditEventType eventType, String userId, String email,
                                  String ipAddress, String userAgent, boolean success, String details) {

            String status = success ? "SUCCESS" : "FAILURE";

            log.info("AUDIT | Type={} | Status={} | UserId={} | Email={} | IP={} | UserAgent={} | Details={}",
                    eventType, status, userId, email, ipAddress, userAgent, details);

            // In production, also send to external audit system:
            // auditEventPublisher.publish(eventType, userId, timestamp, ipAddress, etc.)
        }

        /**
         * Convenience overload for callers (e.g. the domain layer) that don't carry the
         * HTTP context. The client IP / user-agent are resolved here, on the CALLING
         * thread, before the async hop — request-scoped {@link RequestContextHolder}
         * state is not propagated to the audit executor thread.
         */
        public void logAuditEvent(AuditEventType eventType, String userId, String email,
                                  boolean success, String details) {
            logAuditEvent(eventType, userId, email, resolveClientIp(), resolveUserAgent(), success, details);
        }

        private static String resolveClientIp() {
            HttpServletRequest request = currentRequest();
            return request != null ? request.getRemoteAddr() : "unknown";
        }

        private static String resolveUserAgent() {
            HttpServletRequest request = currentRequest();
            if (request == null) {
                return "unknown";
            }
            String ua = request.getHeader("User-Agent");
            return ua != null ? ua : "unknown";
        }

        private static HttpServletRequest currentRequest() {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
                return attrs.getRequest();
            }
            return null;
        }
    }
}
