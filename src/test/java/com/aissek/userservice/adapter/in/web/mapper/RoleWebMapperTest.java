package com.aissek.userservice.adapter.in.web.mapper;

import com.aissek.userservice.adapter.in.web.dto.RoleRequest;
import com.aissek.userservice.adapter.in.web.dto.RoleResponse;
import com.aissek.userservice.domain.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleWebMapperTest {

    private final RoleWebMapper mapper = new RoleWebMapper();

    @Test
    @DisplayName("Should map Role to RoleResponse")
    void toResponseSuccess() {
        Role role = new Role("r1", "ROLE_ADMIN", "Admin");
        RoleResponse response = mapper.toResponse(role);

        assertNotNull(response);
        assertEquals("r1", response.id());
        assertEquals("ROLE_ADMIN", response.name());
    }

    @Test
    @DisplayName("Should map RoleRequest to Role")
    void toDomainSuccess() {
        RoleRequest request = new RoleRequest("ROLE_USER", "User Desc");
        Role role = mapper.toDomain(request);

        assertNotNull(role);
        assertEquals("ROLE_USER", role.getName());
        assertEquals("User Desc", role.getDescription());
    }

    @Test
    @DisplayName("Should handle nulls")
    void handleNulls() {
        assertNull(mapper.toResponse(null));
        assertNull(mapper.toDomain(null));
    }
}
