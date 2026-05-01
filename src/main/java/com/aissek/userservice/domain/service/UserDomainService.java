package com.aissek.userservice.domain.service;

import com.aissek.userservice.domain.exception.*;
import com.aissek.userservice.domain.model.Group;
import com.aissek.userservice.domain.model.User;
import com.aissek.userservice.domain.model.PasswordPolicy;
import com.aissek.userservice.domain.port.in.UserUseCase;
import com.aissek.userservice.domain.port.out.GroupRepositoryPort;
import com.aissek.userservice.domain.port.out.PasswordHasherPort;
import com.aissek.userservice.domain.port.out.UserRepositoryPort;
import com.aissek.userservice.domain.port.out.TokenServicePort;
import com.aissek.userservice.config.AuditConfig.AuditEventType;
import com.aissek.userservice.config.AuditConfig.AuditLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
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
    private final TokenServicePort tokenService;
    private final AuditLogger auditLogger;

    public UserDomainService(UserRepositoryPort userRepository, PasswordHasherPort passwordHasher, GroupRepositoryPort groupRepository, TokenServicePort tokenService, AuditLogger auditLogger){
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.groupRepository = groupRepository;
        this.tokenService = tokenService;
        this.auditLogger = auditLogger;
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
        auditLogger.logAuditEvent(AuditEventType.USER_CREATED, savedUser.getId(), email, 
                true, "User created with " + (groups != null ? groups.size() : 0) + " groups");
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
        User user;
        try {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Authentication failed: email {} not found", email);
                        auditLogger.logAuditEvent(AuditEventType.LOGIN_FAILURE, null, email, 
                                false, "User not found");
                        return new AuthenticationException("Email ou mot de passe invalide");
                    });

            if (!passwordHasher.matches(password, user.getPasswordHash())) {
                log.warn("Authentication failed: invalid password for email {}", email);
                auditLogger.logAuditEvent(AuditEventType.LOGIN_FAILURE, user.getId(), email, 
                        false, "Invalid password");
                throw new AuthenticationException("Email ou mot de passe invalide");
            }

            log.info("User authenticated successfully: ID {}", user.getId());
            auditLogger.logAuditEvent(AuditEventType.LOGIN_SUCCESS, user.getId(), email, 
                    true, "Successful login");
            return user;
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            auditLogger.logAuditEvent(AuditEventType.LOGIN_FAILURE, null, email, 
                    false, "Unexpected error: " + e.getMessage());
            throw e;
        }
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
            auditLogger.logAuditEvent(AuditEventType.PASSWORD_CHANGE_FAILURE, user.getId(), user.getEmail(), 
                    false, "Current password mismatch");
            throw new InvalidDomainStateException("Mot de passe actuel invalide");
        }

        new PasswordPolicy(newPassword);
        user.changePassword(passwordHasher.hash(newPassword));
        userRepository.save(user);
        log.info("Password changed successfully for user ID: {}", id);
        auditLogger.logAuditEvent(AuditEventType.PASSWORD_CHANGE_SUCCESS, user.getId(), user.getEmail(), 
                true, "Password changed successfully");
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        log.info("Deleting user with ID: {}", id);
        User user = getUserById(id);
        auditLogger.logAuditEvent(AuditEventType.USER_DELETED, user.getId(), user.getEmail(), 
                true, "User account deleted");
        userRepository.deleteById(id);
        log.info("User deleted successfully: ID {}", id);
    }

    @Override
    @Transactional
    public void updateRefreshToken(String id, String refreshToken) {
        User user = getUserById(id);
        user.updateRefreshToken(passwordHasher.hash(refreshToken));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User refreshAccessToken(String refreshToken) {
        log.info("Refresh token request received");
        
        // Step 1: Validate JWT signature and expiration FIRST
        String email;
        try {
            email = tokenService.extractUsername(refreshToken);
        } catch (IllegalArgumentException e) {
            log.warn("Refresh token JWT validation failed: {}", e.getMessage());
            auditLogger.logAuditEvent(AuditEventType.TOKEN_REFRESH_FAILURE, null, null, 
                    false, "JWT validation failed: " + e.getMessage());
            throw new AuthenticationException("Invalid or expired refresh token");
        }
        
        // Step 2: Verify the JWT is valid for this username
        if (!tokenService.isTokenValid(refreshToken, email)) {
            log.warn("Refresh token is invalid or expired for user: {}", email);
            auditLogger.logAuditEvent(AuditEventType.TOKEN_REFRESH_FAILURE, null, email, 
                    false, "Token invalid or expired");
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        
        // Step 3: Verify token hash exists in DB (check for revocation/match)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for refresh token email: {}", email);
                    auditLogger.logAuditEvent(AuditEventType.TOKEN_REFRESH_FAILURE, null, email, 
                            false, "User not found");
                    return new AuthenticationException("Invalid or expired refresh token");
                });
        
        if (!passwordHasher.matches(refreshToken, user.getRefreshToken())) {
            log.warn("Refresh token hash mismatch for user: {}", email);
            auditLogger.logAuditEvent(AuditEventType.TOKEN_REFRESH_FAILURE, user.getId(), email, 
                    false, "Token hash mismatch");
            throw new AuthenticationException("Invalid or expired refresh token");
        }
        
        log.info("Refresh token validated successfully for user: {}", user.getEmail());
        auditLogger.logAuditEvent(AuditEventType.TOKEN_REFRESH_SUCCESS, user.getId(), user.getEmail(), 
                true, "Token refreshed successfully");
        return user;
    }
}
