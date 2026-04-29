package com.aissek.userservice.adapter.out.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "myVerySecretKeyForTestingPurposesOnly12345678901234567890";
    private final long expiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", secret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", expiration);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L); // 7 days
    }

    @Test
    @DisplayName("Should generate a valid token for a given username")
    void shouldGenerateValidToken() {
        String username = "test@example.com";
        String token = jwtService.generateToken(username);
        
        assertNotNull(token);
        assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("Should validate a correct token")
    void shouldValidateCorrectToken() {
        String username = "test@example.com";
        String token = jwtService.generateToken(username);
        
        assertTrue(jwtService.isTokenValid(token, username));
    }

    @Test
    @DisplayName("Should generate a refresh token with long expiration")
    void shouldGenerateRefreshToken() {
        String username = "test@example.com";
        String refreshToken = jwtService.generateRefreshToken(username);
        
        assertNotNull(refreshToken);
        assertEquals(username, jwtService.extractUsername(refreshToken));
        // The token should be valid now
        assertTrue(jwtService.isTokenValid(refreshToken, username));
    }

    @Test
    @DisplayName("Should reject a token for a different username")
    void shouldRejectTokenForDifferentUser() {
        String user1 = "user1@example.com";
        String user2 = "user2@example.com";
        String token = jwtService.generateToken(user1);
        
        assertFalse(jwtService.isTokenValid(token, user2));
    }

    @Test
    @DisplayName("Should throw exception for malformed tokens")
    void shouldThrowExceptionForMalformedToken() {
        String malformedToken = "not.a.jwt.token";
        
        assertThrows(IllegalArgumentException.class, () -> jwtService.extractUsername(malformedToken));
    }
}
