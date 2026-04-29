package com.aissek.userservice.domain.service;

import com.aissek.userservice.domain.model.User;
import com.aissek.userservice.domain.port.out.GroupRepositoryPort;
import com.aissek.userservice.domain.port.out.PasswordHasherPort;
import com.aissek.userservice.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDomainServiceTest {

    private UserRepositoryPort userRepository;
    private PasswordHasherPort passwordHasher;
    private GroupRepositoryPort groupRepository;
    private UserDomainService userDomainService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepositoryPort.class);
        passwordHasher = mock(PasswordHasherPort.class);
        groupRepository = mock(GroupRepositoryPort.class);
        userDomainService = new UserDomainService(userRepository, passwordHasher, groupRepository);
    }

    @Test
    @DisplayName("Should update user refresh token successfully")
    void shouldUpdateRefreshToken() {
        User user = new User("John", "john@example.com", "hash", Set.of());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userDomainService.updateRefreshToken(user.getId(), "new-refresh-token");

        assertEquals("new-refresh-token", user.getRefreshToken());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should return user when valid refresh token is provided")
    void shouldRefreshAccessTokenSuccess() {
        User user = new User("John", "john@example.com", "hash", Set.of());
        String token = "valid-token";
        when(userRepository.findByRefreshToken(token)).thenReturn(Optional.of(user));

        User result = userDomainService.refreshAccessToken(token);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("Should throw exception when refresh token is invalid")
    void shouldThrowExceptionWhenRefreshTokenInvalid() {
        when(userRepository.findByRefreshToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(com.aissek.userservice.domain.exception.AuthenticationException.class, 
            () -> userDomainService.refreshAccessToken("invalid-token"));
    }
}
