package com.aissek.userservice.adapter.in.web.mapper;

import com.aissek.userservice.adapter.in.web.dto.RoleRequest;
import com.aissek.userservice.adapter.in.web.dto.RoleResponse;
import com.aissek.userservice.domain.model.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleWebMapper {
    public RoleResponse toResponse(Role role) {
        if (role == null) return null;
        return new RoleResponse(role.getId(), role.getName(), role.getDescription());
    }
    
    public Role toDomain(RoleRequest request) {
        if (request == null) return null;
        return new Role(null, request.name(), request.description());
    }
}
