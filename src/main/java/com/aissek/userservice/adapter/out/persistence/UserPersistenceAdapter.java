package com.aissek.userservice.adapter.out.persistence;

import com.aissek.userservice.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.aissek.userservice.adapter.out.persistence.repository.UserJpaRepository;
import com.aissek.userservice.domain.model.User;
import com.aissek.userservice.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * ADAPTER SORTANT : implémente UserRepositoryPort avec JPA
 * on pourrait remplacer JPA par mongoDB sans toucher au domaine.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepository;

    private final UserPersistenceMapper mapper;

    @Override
    public User save(User user) {
        log.debug("Saving user to database: {}", user.getEmail());
        var saved = jpaRepository.save(mapper.toEntity(user));
        return mapper.toDomaine(saved);
    }

    @Override
    public Optional<User> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDomaine);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomaine);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomaine).toList();
    }

    @Override
    public boolean existByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(String id) {
        log.info("Deleting user from database with ID: {}", id);
        jpaRepository.deleteById(id);
    }
}
