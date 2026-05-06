package com.aissek.userservice.domain.model;

import lombok.Getter;
import java.util.Objects;

/**
 * Role Value Object / Entity.
 * Represents a specific permission set in the system.
 */
@Getter
public class Role {
    private final String id;
    private final String name; // e.g., ROLE_ADMIN, ROLE_USER
    private final String description;

    public Role(String id, String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be blank");
        }
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id) || Objects.equals(name, role.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
