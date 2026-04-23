package com.aissek.userservice.domain.port.out;

/**
 * Port used by the domain to hash and verify passwords.
 */
public interface PasswordHasherPort {

    /**
     * Hashes a raw password before persistence.
     */
    String hash(String rawPassword);

    /**
     * Verifies a raw password against a stored hash.
     */
    boolean matches(String rawPassword, String hashedPassword);
}
