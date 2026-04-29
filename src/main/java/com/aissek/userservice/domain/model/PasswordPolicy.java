package com.aissek.userservice.domain.model;

import java.util.regex.Pattern;

/**
 * Value Object that enforces password complexity rules.
 * Encapsulates the "What makes a password valid" logic.
 */
public record PasswordPolicy(String password) {
    
    private static final int MIN_LENGTH = 8;
    private static final Pattern LETTER_PATTERN = Pattern.compile("[a-zA-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    public PasswordPolicy {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
        if (password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters long");
        }
        if (!LETTER_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one letter");
        }
        if (!DIGIT_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
    }

    public String value() {
        return password;
    }
}
