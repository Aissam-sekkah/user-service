package com.aissek.userservice.domain.service;

import com.aissek.userservice.domain.exception.*;
import com.aissek.userservice.domain.model.Group;
import com.aissek.userservice.domain.model.User;
import com.aissek.userservice.domain.port.in.UserUseCase;
import com.aissek.userservice.domain.port.out.GroupRepositoryPort;
import com.aissek.userservice.domain.port.out.PasswordHasherPort;
import com.aissek.userservice.domain.port.out.UserRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * SERVICE DOMAIN : logique métier pure
 * Dépend uniquement des ports (interfaces), jamais des adapters.
 */
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
        // Règle métier : Email unique
        if(userRepository.existByEmail(email))
            throw new ConflictException("Email déjà utilisé : " + email);

        User user = new User(name, email, passwordHasher.hash(password), groups);
        return userRepository.save(user);
    }

    @Override
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found : " + id));
    }

    @Override
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Email ou mot de passe invalide"));

        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException("Email ou mot de passe invalide");
        }

        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User updateUser(String id, String name, String email, Set<Group> groups) {
        User user = getUserById(id);
        user.updateProfile(name, email, groups);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(String id, String currentPassword, String newPassword) {
        User user = getUserById(id);
        if (!passwordHasher.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidDomainStateException("Mot de passe actuel invalide");
        }

        user.changePassword(passwordHasher.hash(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        getUserById(id);
        userRepository.deleteById(id);
    }
}
