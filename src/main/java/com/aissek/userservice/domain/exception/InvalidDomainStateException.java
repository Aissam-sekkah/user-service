package com.aissek.userservice.domain.exception;

public class InvalidDomainStateException extends DomainException {
    public InvalidDomainStateException(String message) {
        super(message);
    }
}
