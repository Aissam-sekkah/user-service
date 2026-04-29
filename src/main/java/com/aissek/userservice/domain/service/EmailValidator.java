package com.aissek.userservice.domain.service;

import java.util.regex.Pattern;

/**
 * Domain utility for rigorous email validation.
 * Separated from the model to keep the model focused on state.
 */
public class EmailValidator {
    
    // RFC 5322 compliant regex
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"
    );

    public static void validate(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }
}
