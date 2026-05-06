package com.aissek.userservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserDomainModelTest {

    @Test
    @DisplayName("Should calculate effective roles from both direct and group assignments")
    void shouldCalculateEffectiveRoles() {
        // Arrange
        Role roleUser = new Role("1", "ROLE_USER", "User role");
        Role roleAdmin = new Role("2", "ROLE_ADMIN", "Admin role");
        Role roleManager = new Role("3", "ROLE_MANAGER", "Manager role");

        Group groupDev = new Group("Developers", "Dev Group");
        groupDev.assignRoles(Set.of(roleUser, roleManager));

        User user = new User("John", "john@example.com", "hash", Set.of(groupDev));
        // Direct assignment of ADMIN
        user.assignDirectRoles(Set.of(roleAdmin));

        // Act
        Set<Role> effectiveRoles = user.getEffectiveRoles();

        // Assert
        assertEquals(3, effectiveRoles.size(), "User should have 3 effective roles (1 direct + 2 from group)");
        assertTrue(effectiveRoles.contains(roleUser));
        assertTrue(effectiveRoles.contains(roleAdmin));
        assertTrue(effectiveRoles.contains(roleManager));
    }

    @Test
    @DisplayName("Should handle users with no roles and no groups")
    void shouldHandleNoRoles() {
        User user = new User("John", "john@example.com", "hash", null);
        
        Set<Role> effectiveRoles = user.getEffectiveRoles();
        
        assertNotNull(effectiveRoles);
        assertTrue(effectiveRoles.isEmpty());
    }

    @Test
    @DisplayName("Should avoid duplicate roles when user and group share the same role")
    void shouldAvoidDuplicateRoles() {
        Role roleUser = new Role("1", "ROLE_USER", "User role");
        
        Group groupGeneral = new Group("General", "Desc");
        groupGeneral.assignRoles(Set.of(roleUser));
        
        User user = new User("John", "john@example.com", "hash", Set.of(groupGeneral));
        user.assignDirectRoles(Set.of(roleUser)); // Same role assigned directly

        Set<Role> effectiveRoles = user.getEffectiveRoles();

        assertEquals(1, effectiveRoles.size(), "Duplicate roles should be merged into one");
    }
}
