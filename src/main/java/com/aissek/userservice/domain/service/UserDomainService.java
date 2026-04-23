package com.aissek.userservice.domain.service;

import com.aissek.userservice.domain.model.User;
import com.aissek.userservice.domain.port.in.UserUseCase;
import com.aissek.userservice.domain.port.out.PasswordHasherPort;
import com.aissek.userservice.domain.port.out.UserRepositoryPort;

import java.util.List;

/**
 * SERVICE DOMAIN : logique métier pure
 * Dépend uniquement des ports (interfaces), jamais des adapters.
 */
public class UserDomainService implements UserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;

    public UserDomainService(UserRepositoryPort userRepository, PasswordHasherPort passwordHasher){
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public User createUser(String name, String email, String password) {
        // Règle métier : Email unique
        if(userRepository.existByEmail(email))
            throw new UserEmailAlreadyExistsException("Email déjà utilisé : " + email);

        User user = new User(name, email, passwordHasher.hash(password));
        return userRepository.save(user);
    }

    @Override
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found : " + id));
    }

    @Override
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationFailedException("Email ou mot de passe invalide"));

        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new AuthenticationFailedException("Email ou mot de passe invalide");
        }

        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(String id, String name, String email) {
        User user = getUserById(id);
        user.updateProfile(name, email);
        return userRepository.save(user);
    }

    @Override
    public void changePassword(String id, String currentPassword, String newPassword) {
        User user = getUserById(id);
        if (!passwordHasher.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidPasswordException("Mot de passe actuel invalide");
        }

        user.changePassword(passwordHasher.hash(newPassword));
        userRepository.save(user);
    }

    @Override
    public void deleteUser(String id) {
        getUserById(id);
        userRepository.deleteById(id);
    }

    public static class UserEmailAlreadyExistsException extends RuntimeException {
        public UserEmailAlreadyExistsException(String email) {
            super(email);
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidPasswordException extends RuntimeException {
        public InvalidPasswordException(String message) {
            super(message);
        }
    }

    public static class AuthenticationFailedException extends RuntimeException {
        public AuthenticationFailedException(String message) {
            super(message);
        }
    }
}
