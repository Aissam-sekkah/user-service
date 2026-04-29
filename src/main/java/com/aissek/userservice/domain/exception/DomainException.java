package com.aissek.userservice.domain.exception;

/**
 * Base exception for all business rule violations.
 * Using a RuntimeException to avoid polluting the UseCase interface signatures.
 */
public abstract class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
