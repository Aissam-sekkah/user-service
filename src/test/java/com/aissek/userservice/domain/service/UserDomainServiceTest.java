package com.aissek.userservice.domain.service;

import com.aissek.userservice.adapter.out.security.JwtService;
import com.aissek.userservice.config.AuditConfig.AuditLogger;
import com.aissek.userservice.domain.model.User;
import com.aissek.userservice.domain.port.out.GroupRepositoryPort;
import com.aissek.userservice.domain.port.out.PasswordHasherPort;
import com.aissek.userservice.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDomainServiceTest {

    private UserRepositoryPort userRepository;
    private PasswordHasherPort passwordHasher;
    private GroupRepositoryPort groupRepository;
    private JwtService jwtService;
    private AuditLogger auditLogger;
    private UserDomainService userDomainService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepositoryPort.class);
        passwordHasher = mock(PasswordHasherPort.class);
        groupRepository = mock(GroupRepositoryPort.class);
        jwtService = mock(JwtService.class);
        auditLogger = mock(AuditLogger.class);
        userDomainService = new UserDomainService(userRepository, passwordHasher, groupRepository, jwtService, auditLogger);
    }

    @Test
    @DisplayName("Should update user refresh token successfully")
    void shouldUpdateRefreshToken() {
        User user = new User("John", "john@example.com", "hash", Set.of());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordHasher.hash("new-refresh-token")).thenReturn("hashed-token");

        userDomainService.updateRefreshToken(user.getId(), "new-refresh-token");

        assertEquals("hashed-token", user.getRefreshToken());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should return user when valid refresh token is provided")
    void shouldRefreshAccessTokenSuccess() {
        User user = new User("John", "john@example.com", "hash", Set.of());
        user.updateRefreshToken("hashed-token"); // Simulate hashed token in DB
        String token = "valid-token";
        
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.extractUsername(token)).thenReturn(user.getEmail());
        when(jwtService.isTokenValid(token, user.getEmail())).thenReturn(true);
        when(passwordHasher.matches(token, "hashed-token")).thenReturn(true);

        User result = userDomainService.refreshAccessToken(token);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("Should throw exception when refresh token is invalid")
    void shouldThrowExceptionWhenRefreshTokenInvalid() {
        String invalidToken = "invalid-token";
        when(jwtService.extractUsername(invalidToken)).thenThrow(new IllegalArgumentException("Invalid JWT token"));

        assertThrows(com.aissek.userservice.domain.exception.AuthenticationException.class, 
            () -> userDomainService.refreshAccessToken(invalidToken));
    }

    @Test
    @DisplayName("Should throw exception when JWT validation fails")
    void shouldThrowExceptionWhenJwtValidationFails() {
        String expiredToken = "expired-token";
        when(jwtService.extractUsername(expiredToken)).thenReturn("test@example.com");
        when(jwtService.isTokenValid(expiredToken, "test@example.com")).thenReturn(false);

        assertThrows(com.aissek.userservice.domain.exception.AuthenticationException.class, 
            () -> userDomainService.refreshAccessToken(expiredToken));
    }

    @Test
    @DisplayName("Should throw exception when refresh token not found in database")
    void shouldThrowExceptionWhenTokenNotFoundInDatabase() {
        String token = "valid-jwt-but-not-in-db";
        String email = "test@example.com";
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(jwtService.isTokenValid(token, email)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(com.aissek.userservice.domain.exception.AuthenticationException.class, 
            () -> userDomainService.refreshAccessToken(token));
    }
}
