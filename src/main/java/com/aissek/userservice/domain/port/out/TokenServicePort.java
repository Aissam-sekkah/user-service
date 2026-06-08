package com.aissek.userservice.domain.port.out;

/**
 * Port for token management.
 * Decouples the domain from specific JWT implementations.
 */
public interface TokenServicePort {
    String extractUsername(String token);
    boolean isTokenValid(String token, String username);
    String generateToken(String username);
    String generateRefreshToken(String username);
}
