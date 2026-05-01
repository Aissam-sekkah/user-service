package com.aissek.userservice.adapter.out.persistence;

import com.aissek.userservice.adapter.out.persistence.entity.RoleEntity;
import com.aissek.userservice.adapter.out.persistence.mapper.RolePersistenceMapper;
import com.aissek.userservice.adapter.out.persistence.repository.RoleJpaRepository;
import com.aissek.userservice.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolePersistenceAdapterTest {

    @Mock private RoleJpaRepository roleJpaRepository;
    @Mock private RolePersistenceMapper roleMapper;

    @InjectMocks private RolePersistenceAdapter adapter;

    private Role role;
    private RoleEntity entity;

    @BeforeEach
    void setUp() {
        role = new Role("r1", "ROLE_USER", "User");
        entity = new RoleEntity();
        entity.setId("r1");
        entity.setName("ROLE_USER");
        entity.setDescription("User");
    }

    @Test
    @DisplayName("Should save role and return domain model")
    void saveSuccess() {
        when(roleMapper.toEntity(role)).thenReturn(entity);
        when(roleJpaRepository.save(entity)).thenReturn(entity);
        when(roleMapper.toDomain(entity)).thenReturn(role);

        Role result = adapter.save(role);

        assertNotNull(result);
        assertEquals("ROLE_USER", result.getName());
        verify(roleJpaRepository).save(entity);
    }

    @Test
    @DisplayName("Should find role by id")
    void findByIdSuccess() {
        when(roleJpaRepository.findById("r1")).thenReturn(Optional.of(entity));
        when(roleMapper.toDomain(entity)).thenReturn(role);

        Optional<Role> result = adapter.findById("r1");

        assertTrue(result.isPresent());
        assertEquals("ROLE_USER", result.get().getName());
    }

    @Test
    @DisplayName("Should find role by name")
    void findByNameSuccess() {
        when(roleJpaRepository.findByName("ROLE_USER")).thenReturn(Optional.of(entity));
        when(roleMapper.toDomain(entity)).thenReturn(role);

        Optional<Role> result = adapter.findByName("ROLE_USER");

        assertTrue(result.isPresent());
        assertEquals("ROLE_USER", result.get().getName());
    }

    @Test
    @DisplayName("Should return all roles")
    void findAllSuccess() {
        when(roleJpaRepository.findAll()).thenReturn(List.of(entity));
        when(roleMapper.toDomain(entity)).thenReturn(role);

        List<Role> result = adapter.findAll();

        assertEquals(1, result.size());
        assertEquals("ROLE_USER", result.get(0).getName());
    }
}
