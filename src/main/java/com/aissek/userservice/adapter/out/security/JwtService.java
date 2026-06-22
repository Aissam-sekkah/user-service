package com.aissek.userservice.adapter.out.security;

import com.aissek.userservice.domain.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import jakarta.annotation.PostConstruct;

/**
 * JWT Utility Service for token lifecycle management.
 * Handles creation, extraction, and validation of tokens.
 */
@Slf4j
@Service
public class JwtService implements com.aissek.userservice.domain.port.out.TokenServicePort {

    @Value("${application.security.jwt.secret}")
    private String secretKey;

    @Value("${application.security.jwt.expiration:3600000}") // 1 hour
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-expiration:604800000}") // 7 days
    private long refreshExpiration;

    @PostConstruct
    public void validateSecretKey() {
        if (secretKey == null || secretKey.isBlank() || secretKey.length() < 32) {
            log.error("JWT_SECRET validation failed: key must be set and at least 32 characters");
            throw new IllegalStateException("JWT_SECRET must be set as an environment variable and be at least 32 characters long for security");
        }
        log.info("JWT secret key validated successfully (length: {})", secretKey.length());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(String username) {
        Map<String, Object> extraClaims = new HashMap<>();
        // We'll handle roles outside this method or pass them in
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isTokenValid(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (InvalidTokenException e) {
            // Token is invalid or expired
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            throw new InvalidTokenException("JWT token has expired");
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.warn("JWT token unsupported: {}", e.getMessage());
            throw new InvalidTokenException("JWT token is unsupported");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("JWT token malformed: {}", e.getMessage());
            throw new InvalidTokenException("JWT token is malformed");
        } catch (JwtException e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            throw new InvalidTokenException("Invalid JWT token");
        } catch (Exception e) {
            log.error("Unexpected error during JWT validation: {}", e.getMessage());
            throw new InvalidTokenException("Invalid JWT token");
        }
    }
}
