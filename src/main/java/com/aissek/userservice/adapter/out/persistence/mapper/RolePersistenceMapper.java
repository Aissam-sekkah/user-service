package com.aissek.userservice.adapter.out.persistence.mapper;

import com.aissek.userservice.adapter.out.persistence.entity.RoleEntity;
import com.aissek.userservice.domain.model.Role;
import org.springframework.stereotype.Component;

@Component
public class RolePersistenceMapper {

    public RoleEntity toEntity(Role role) {
        if (role == null) return null;
        RoleEntity entity = new RoleEntity();
        entity.setId(role.getId());
        entity.setName(role.getName());
        entity.setDescription(role.getDescription());
        return entity;
    }

    public Role toDomain(RoleEntity entity) {
        if (entity == null) return null;
        return new Role(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }
}
