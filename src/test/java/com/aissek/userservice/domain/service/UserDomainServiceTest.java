package com.aissek.userservice.domain.service;

import com.aissek.userservice.domain.model.User;
import com.aissek.userservice.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDomainServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private UserDomainService userDomainService;

    private User user;
    private String name;
    private String email;

    @BeforeEach
    void setUp(){
        name = "ali";
        email = "ali@email.com";
        user = new User("123", "ali", "ali@email.com", LocalDateTime.now());
    }

    @Test
    @DisplayName("Test de création d'un nouveau user")
    public void shouldCreateUserSuccessfully(){
        // Arrange
        when(userRepository.existByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = userDomainService.createUser(name, email);

        // Assert
        verify(userRepository, times(1) ).save(any(User.class));
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("ali@email.com");

    }

    @Test
    @DisplayName("Test de creation d'utilisateur avec un email existant")
    public void shouldThrowEmailAlreadyExist(){
        // Arrange
        when(userRepository.existByEmail(anyString())).thenReturn(true);
        // Act && Assert
        assertThatThrownBy(() -> userDomainService.createUser(name, email))
                .isInstanceOf(UserDomainService.UserEmailAlreadyExistsException.class)
                .hasMessageContaining("Email déjà utilisé" );
    }
}
