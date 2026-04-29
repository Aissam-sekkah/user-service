package com.aissek.userservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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
         * Logs security audit events asynchronously.
         * Includes: timestamp, event type, user identifier, IP address, user agent.
         */
        @Async
        public void logAuditEvent(AuditEventType eventType, String userId, String email, 
                                  String ipAddress, String userAgent, boolean success, String details) {
            
            String status = success ? "SUCCESS" : "FAILURE";
            
            log.info("AUDIT | Type={} | Status={} | UserId={} | Email={} | IP={} | Details={}",
                    eventType, status, userId, email, ipAddress, details);
            
            // In production, also send to external audit system:
            // auditEventPublisher.publish(eventType, userId, timestamp, ipAddress, etc.)
        }

        /**
         * Simplified version without IP/user agent (for domain layer calls).
         */
        @Async
        public void logAuditEvent(AuditEventType eventType, String userId, String email, 
                                  boolean success, String details) {
            logAuditEvent(eventType, userId, email, "unknown", "unknown", success, details);
        }
    }
}
