package com.aissek.userservice.domain.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hashing for long, high-entropy, opaque tokens (e.g. JWT refresh tokens).
 *
 * <p>bcrypt is intentionally NOT used here: it only considers the first 72 bytes
 * of its input, and a JWT shares a near-constant header/prefix, so a bcrypt hash
 * of a refresh token effectively validates only a truncated slice. Refresh tokens
 * already carry full cryptographic entropy from the signing key, so a fast digest
 * (SHA-256) is the correct primitive — no salt/stretching needed.
 */
public final class TokenHasher {

    private TokenHasher() {
    }

    public static String sha256(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token to hash cannot be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by the JDK; this never happens.
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }

    /**
     * Constant-time comparison of a raw token against a previously stored hash.
     */
    public static boolean matches(String rawToken, String storedHash) {
        if (rawToken == null || storedHash == null) {
            return false;
        }
        byte[] expected = storedHash.getBytes(StandardCharsets.UTF_8);
        byte[] actual = sha256(rawToken).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }
}
