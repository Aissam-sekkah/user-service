package com.aissek.userservice.domain.service;

import java.util.regex.Pattern;

/**
 * Domain utility for rigorous email validation.
 * Separated from the model to keep the model focused on state.
 */
public class EmailValidator {
    
    // Pragmatic email validation: non-empty local part (no leading/trailing/consecutive dots),
    // a domain, and a TLD of at least two letters. Rejects inputs like "a@b" or "user@host".
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*" +
        "@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)*" +
        "\\.[A-Za-z]{2,}$"
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
