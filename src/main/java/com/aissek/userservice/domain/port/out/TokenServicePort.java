package com.aissek.userservice.domain.port.out;

import java.util.Set;

/**
 * Port for token management.
 * Decouples the domain from specific JWT implementations.
 */
public interface TokenServicePort {
    String extractUsername(String token);
    boolean isTokenValid(String token, String username);
    String generateToken(String username);
    String generateToken(String username, Set<String> roles);
    String generateRefreshToken(String username);
}
