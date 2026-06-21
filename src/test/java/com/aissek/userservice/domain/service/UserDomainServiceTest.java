package com.aissek.userservice.domain.service;

import com.aissek.userservice.config.AuditConfig.AuditLogger;
import com.aissek.userservice.domain.model.User;
import com.aissek.userservice.domain.port.out.GroupRepositoryPort;
import com.aissek.userservice.domain.port.out.PasswordHasherPort;
import com.aissek.userservice.domain.port.out.UserRepositoryPort;
import com.aissek.userservice.domain.port.out.TokenServicePort;
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
    private TokenServicePort tokenService;
    private AuditLogger auditLogger;
    private UserDomainService userDomainService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepositoryPort.class);
        passwordHasher = mock(PasswordHasherPort.class);
        groupRepository = mock(GroupRepositoryPort.class);
        tokenService = mock(TokenServicePort.class);
        auditLogger = mock(AuditLogger.class);
        userDomainService = new UserDomainService(userRepository, passwordHasher, groupRepository, tokenService, auditLogger);
    }

    @Test
    @DisplayName("Should update user refresh token successfully")
    void shouldUpdateRefreshToken() {
        User user = new User("John", "john@example.com", "hash", Set.of());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userDomainService.updateRefreshToken(user.getId(), "new-refresh-token");

        // Refresh tokens are stored as SHA-256 hashes (not bcrypt).
        assertEquals(TokenHasher.sha256("new-refresh-token"), user.getRefreshToken());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should return user when valid refresh token is provided")
    void shouldRefreshAccessTokenSuccess() {
        User user = new User("John", "john@example.com", "hash", Set.of());
        String token = "valid-token";
        user.updateRefreshToken(TokenHasher.sha256(token)); // Simulate stored SHA-256 hash

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenService.extractUsername(token)).thenReturn(user.getEmail());
        when(tokenService.isTokenValid(token, user.getEmail())).thenReturn(true);

        User result = userDomainService.refreshAccessToken(token);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("Should throw exception when refresh token is invalid")
    void shouldThrowExceptionWhenRefreshTokenInvalid() {
        String invalidToken = "invalid-token";
        when(tokenService.extractUsername(invalidToken)).thenThrow(new IllegalArgumentException("Invalid JWT token"));

        assertThrows(com.aissek.userservice.domain.exception.AuthenticationException.class, 
            () -> userDomainService.refreshAccessToken(invalidToken));
    }

    @Test
    @DisplayName("Should throw exception when JWT validation fails")
    void shouldThrowExceptionWhenJwtValidationFails() {
        String expiredToken = "expired-token";
        when(tokenService.extractUsername(expiredToken)).thenReturn("test@example.com");
        when(tokenService.isTokenValid(expiredToken, "test@example.com")).thenReturn(false);

        assertThrows(com.aissek.userservice.domain.exception.AuthenticationException.class, 
            () -> userDomainService.refreshAccessToken(expiredToken));
    }

    @Test
    @DisplayName("Should lock account after 5 consecutive failed logins")
    void shouldLockAccountAfterMaxFailedAttempts() {
        User user = new User("John", "john@example.com", "hash", Set.of());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong", "hash")).thenReturn(false);

        for (int i = 0; i < 5; i++) {
            assertThrows(com.aissek.userservice.domain.exception.AuthenticationException.class,
                    () -> userDomainService.login(user.getEmail(), "wrong"));
        }

        assertTrue(user.isLocked());
        assertEquals(5, user.getFailedLoginAttempts());
        verify(userRepository, times(5)).save(user);
    }

    @Test
    @DisplayName("Should reject login while account is locked, even with correct password")
    void shouldRejectLoginWhenAccountLocked() {
        User user = new User("John", "john@example.com", "hash", Set.of());
        user.recordFailedLogin(1, java.time.Duration.ofMinutes(15)); // force lock
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(com.aissek.userservice.domain.exception.AuthenticationException.class,
                () -> userDomainService.login(user.getEmail(), "hash"));
        // Password never checked when locked.
        verify(passwordHasher, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should reset failed attempts on successful login")
    void shouldResetFailedAttemptsOnSuccess() {
        User user = new User("John", "john@example.com", "hash", Set.of());
        user.recordFailedLogin(10, java.time.Duration.ofMinutes(15)); // 1 failure, not locked
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordHasher.matches("hash", "hash")).thenReturn(true);

        userDomainService.login(user.getEmail(), "hash");

        assertEquals(0, user.getFailedLoginAttempts());
        assertFalse(user.isLocked());
    }

    @Test
    @DisplayName("Should throw exception when refresh token not found in database")
    void shouldThrowExceptionWhenTokenNotFoundInDatabase() {
        String token = "valid-jwt-but-not-in-db";
        String email = "test@example.com";
        when(tokenService.extractUsername(token)).thenReturn(email);
        when(tokenService.isTokenValid(token, email)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(com.aissek.userservice.domain.exception.AuthenticationException.class, 
            () -> userDomainService.refreshAccessToken(token));
    }
}
