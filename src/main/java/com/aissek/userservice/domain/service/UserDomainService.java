package com.aissek.userservice.domain.service;

import com.aissek.userservice.domain.model.User;
import com.aissek.userservice.domain.port.in.UserUseCase;
import com.aissek.userservice.domain.port.out.UserRepositoryPort;

import java.util.List;

/**
 * SERVICE DOMAIN : logique métier pure
 * Dépend uniquement des ports (interfaces), jamais des adapters.
 */
public class UserDomainService implements UserUseCase {

    private final UserRepositoryPort userRepository;

    public UserDomainService(UserRepositoryPort userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(String name, String email) {
        // Règle métier : Email unique
        if(userRepository.existByEmail(email))
            throw new UserEmailAlreadyExistsException("Email déjà utilisé : " + email);

        User user = new User(name, email);
        return userRepository.save(user);
    }

    @Override
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("User not found : " + id));
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
    public void deleteUser(String id) {
        getUserById(id);
        userRepository.deleteById(id);
    }

    public static class UserEmailAlreadyExistsException extends RuntimeException {
        public UserEmailAlreadyExistsException(String email) {
            super(email);
        }
    }
}
