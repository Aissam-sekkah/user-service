package com.aissek.userservice.adapter.out.persistence.mapper;

import com.aissek.userservice.adapter.out.persistence.entity.RoleEntity;
import com.aissek.userservice.domain.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RolePersistenceMapperTest {

    private final RolePersistenceMapper mapper = new RolePersistenceMapper();

    @Test
    @DisplayName("Should map Role domain model to RoleEntity")
    void shouldMapToEntity() {
        Role role = new Role("id-123", "ROLE_ADMIN", "Administrator");
        
        RoleEntity entity = mapper.toEntity(role);
        
        assertNotNull(entity);
        assertEquals("id-123", entity.getId());
        assertEquals("ROLE_ADMIN", entity.getName());
        assertEquals("Administrator", entity.getDescription());
    }

    @Test
    @DisplayName("Should map RoleEntity to Role domain model")
    void shouldMapToDomain() {
        RoleEntity entity = new RoleEntity();
        entity.setId("id-123");
        entity.setName("ROLE_USER");
        entity.setDescription("Standard User");
        
        Role role = mapper.toDomain(entity);
        
        assertNotNull(role);
        assertEquals("id-123", role.getId());
        assertEquals("ROLE_USER", role.getName());
        assertEquals("Standard User", role.getDescription());
    }

    @Test
    @DisplayName("Should return null when mapping null")
    void shouldHandleNulls() {
        assertNull(mapper.toEntity(null));
        assertNull(mapper.toDomain(null));
    }
}
