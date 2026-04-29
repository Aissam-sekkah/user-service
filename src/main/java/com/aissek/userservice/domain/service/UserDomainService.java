package com.aissek.userservice.domain.service;

import com.aissek.userservice.domain.exception.*;
import com.aissek.userservice.domain.model.Group;
import com.aissek.userservice.domain.model.User;
import com.aissek.userservice.domain.model.PasswordPolicy;
import com.aissek.userservice.domain.port.in.UserUseCase;
import com.aissek.userservice.domain.port.out.GroupRepositoryPort;
import com.aissek.userservice.domain.port.out.PasswordHasherPort;
import com.aissek.userservice.domain.port.out.UserRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * SERVICE DOMAIN : logique métier pure
 * Dépend uniquement des ports (interfaces), jamais des adapters.
 */
@Slf4j
@Transactional(readOnly = true)
public class UserDomainService implements UserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final GroupRepositoryPort groupRepository;

    public UserDomainService(UserRepositoryPort userRepository, PasswordHasherPort passwordHasher, GroupRepositoryPort groupRepository){
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.groupRepository = groupRepository;
    }

    @Override
    @Transactional
    public User createUser(String name, String email, String password, Set<Group> groups) {
        log.info("Attempting to create new user with email: {}", email);
        new PasswordPolicy(password);
        
        if(userRepository.existByEmail(email)) {
            log.warn("User creation failed: email {} already exists", email);
            throw new ConflictException("Email déjà utilisé : " + email);
        }

        User user = new User(name, email, passwordHasher.hash(password), groups);
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User lookup failed: ID {} not found", id);
                    return new ResourceNotFoundException("User not found : " + id);
                });
    }

    @Override
    public User login(String email, String password) {
        log.info("Authentication attempt for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Authentication failed: email {} not found", email);
                    return new AuthenticationException("Email ou mot de passe invalide");
                });

        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            log.warn("Authentication failed: invalid password for email {}", email);
            throw new AuthenticationException("Email ou mot de passe invalide");
        }

        log.info("User authenticated successfully: ID {}", user.getId());
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User updateUser(String id, String name, String email, Set<Group> groups) {
        log.info("Updating profile for user ID: {}", id);
        User user = getUserById(id);
        user.updateProfile(name, email, groups);
        User updatedUser = userRepository.save(user);
        log.info("User profile updated successfully: ID {}", updatedUser.getId());
        return updatedUser;
    }

    @Override
    @Transactional
    public void changePassword(String id, String currentPassword, String newPassword) {
        log.info("Password change requested for user ID: {}", id);
        User user = getUserById(id);
        if (!passwordHasher.matches(currentPassword, user.getPasswordHash())) {
            log.warn("Password change failed: current password mismatch for user ID {}", id);
            throw new InvalidDomainStateException("Mot de passe actuel invalide");
        }

        new PasswordPolicy(newPassword);
        user.changePassword(passwordHasher.hash(newPassword));
        userRepository.save(user);
        log.info("Password changed successfully for user ID: {}", id);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        log.info("Deleting user with ID: {}", id);
        getUserById(id);
        userRepository.deleteById(id);
        log.info("User deleted successfully: ID {}", id);
    }
}
