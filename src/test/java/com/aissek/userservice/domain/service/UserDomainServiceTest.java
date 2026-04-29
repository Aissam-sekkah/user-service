package com.aissek.userservice.domain.service;

import com.aissek.userservice.domain.exception.*;
import com.aissek.userservice.domain.model.User;
import com.aissek.userservice.domain.port.out.PasswordHasherPort;
import com.aissek.userservice.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDomainServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @InjectMocks
    private UserDomainService userDomainService;

    private User user;
    private String name;
    private String email;
    private String password;
    private String passwordHash;

    @BeforeEach
    void setUp(){
        name = "ali";
        email = "ali@email.com";
        password = "Password123!";
        passwordHash = "$2a$10$hashedPassword";
        user = new User("123", "ali", "ali@email.com", passwordHash, null, LocalDateTime.now());
    }

    @Test
    @DisplayName("Test de création d'un nouveau user")
    public void shouldCreateUserSuccessfully(){
        // Arrange
        when(userRepository.existByEmail(anyString())).thenReturn(false);
        when(passwordHasherPort.hash(password)).thenReturn(passwordHash);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = userDomainService.createUser(name, email, password, null);

        // Assert
        verify(userRepository, times(1) ).save(any(User.class));
        verify(passwordHasherPort, times(1)).hash(password);
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("ali@email.com");

    }

    @Test
    @DisplayName("Test de creation d'utilisateur avec un email existant")
    public void shouldThrowEmailAlreadyExist(){
        // Arrange
        when(userRepository.existByEmail(anyString())).thenReturn(true);
        // Act && Assert
        assertThatThrownBy(() -> userDomainService.createUser(name, email, password, null))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email déjà utilisé" );
    }

    @Test
    @DisplayName("Test de changement de mot de passe")
    void shouldChangePasswordSuccessfully() {
        String newPassword = "NewPassword123!";
        String newPasswordHash = "$2a$10$newHashedPassword";

        when(userRepository.findById("123")).thenReturn(java.util.Optional.of(user));
        when(passwordHasherPort.matches(password, passwordHash)).thenReturn(true);
        when(passwordHasherPort.hash(newPassword)).thenReturn(newPasswordHash);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userDomainService.changePassword("123", password, newPassword);

        verify(passwordHasherPort).matches(password, passwordHash);
        verify(passwordHasherPort).hash(newPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Test de changement de mot de passe avec mot de passe actuel invalide")
    void shouldThrowWhenCurrentPasswordIsInvalid() {
        when(userRepository.findById("123")).thenReturn(java.util.Optional.of(user));
        when(passwordHasherPort.matches("wrong-password", passwordHash)).thenReturn(false);

        assertThatThrownBy(() -> userDomainService.changePassword("123", "wrong-password", "newPassword123"))
                .isInstanceOf(InvalidDomainStateException.class)
                .hasMessageContaining("Mot de passe actuel invalide");
    }

    @Test
    @DisplayName("Test de login réussi")
    void shouldLoginSuccessfully() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches(password, passwordHash)).thenReturn(true);

        User result = userDomainService.login(email, password);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        verify(userRepository).findByEmail(email);
        verify(passwordHasherPort).matches(password, passwordHash);
    }

    @Test
    @DisplayName("Test de login avec email inconnu")
    void shouldThrowWhenLoginEmailDoesNotExist() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDomainService.login(email, password))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Email ou mot de passe invalide");
    }

    @Test
    @DisplayName("Test de login avec mot de passe invalide")
    void shouldThrowWhenLoginPasswordIsInvalid() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("wrong-password", passwordHash)).thenReturn(false);

        assertThatThrownBy(() -> userDomainService.login(email, "wrong-password"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Email ou mot de passe invalide");
    }
}
