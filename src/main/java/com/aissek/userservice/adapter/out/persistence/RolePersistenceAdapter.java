package com.aissek.userservice.adapter.out.persistence;

import com.aissek.userservice.adapter.out.persistence.entity.RoleEntity;
import com.aissek.userservice.adapter.out.persistence.mapper.RolePersistenceMapper;
import com.aissek.userservice.adapter.out.persistence.repository.RoleJpaRepository;
import com.aissek.userservice.domain.model.Role;
import com.aissek.userservice.domain.port.out.RoleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RolePersistenceAdapter implements RoleRepositoryPort {

    private final RoleJpaRepository roleJpaRepository;
    private final RolePersistenceMapper roleMapper;

    @Override
    public Role save(Role role) {
        RoleEntity entity = roleMapper.toEntity(role);
        return roleMapper.toDomain(roleJpaRepository.save(entity));
    }

    @Override
    public Optional<Role> findById(String id) {
        return roleJpaRepository.findById(id).map(roleMapper::toDomain);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return roleJpaRepository.findByName(name).map(roleMapper::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return roleJpaRepository.findAll().stream()
                .map(roleMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        roleJpaRepository.deleteById(id);
    }
}
