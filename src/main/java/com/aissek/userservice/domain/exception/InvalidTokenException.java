package com.aissek.userservice.domain.exception;

/**
 * Raised when a JWT (access or refresh) is malformed, unsupported, expired or has
 * an invalid signature. Mapped to HTTP 401 by the web layer — a dedicated type
 * replaces fragile {@code message.contains("JWT")} routing.
 */
public class InvalidTokenException extends DomainException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
