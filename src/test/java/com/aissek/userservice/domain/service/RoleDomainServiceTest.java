package com.aissek.userservice.domain.service;

import com.aissek.userservice.domain.exception.ConflictException;
import com.aissek.userservice.domain.exception.ResourceNotFoundException;
import com.aissek.userservice.domain.model.Group;
import com.aissek.userservice.domain.model.Role;
import com.aissek.userservice.domain.model.User;
import com.aissek.userservice.domain.port.out.GroupRepositoryPort;
import com.aissek.userservice.domain.port.out.RoleRepositoryPort;
import com.aissek.userservice.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleDomainServiceTest {

    @Mock private RoleRepositoryPort roleRepository;
    @Mock private UserRepositoryPort userRepository;
    @Mock private GroupRepositoryPort groupRepository;

    @InjectMocks private RoleDomainService roleDomainService;

    private Role adminRole;
    private User testUser;
    private Group testGroup;

    @BeforeEach
    void setUp() {
        adminRole = new Role("role-1", "ROLE_ADMIN", "Admin Role");
        testUser = new User("John", "john@example.com", "hash", null);
        testGroup = new Group("Group A", "Desc A");
    }

    @Test
    @DisplayName("Should create role successfully when name is unique")
    void createRoleSuccess() {
        when(roleRepository.findByName("ROLE_NEW")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(i -> i.getArguments()[0]);

        Role created = roleDomainService.createRole("ROLE_NEW", "Description");

        assertNotNull(created);
        assertEquals("ROLE_NEW", created.getName());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when role name already exists")
    void createRoleConflict() {
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));

        assertThrows(ConflictException.class, () -> roleDomainService.createRole("ROLE_ADMIN", "Desc"));
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should assign role to user successfully")
    void assignRoleToUserSuccess() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(testUser));
        when(roleRepository.findById("r1")).thenReturn(Optional.of(adminRole));

        roleDomainService.assignRoleToUser("u1", "r1");

        assertTrue(testUser.getDirectRoles().contains(adminRole));
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when assigning non-existent role to user")
    void assignRoleToUserRoleNotFound() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(testUser));
        when(roleRepository.findById("r1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roleDomainService.assignRoleToUser("u1", "r1"));
    }

    @Test
    @DisplayName("Should assign role to group successfully")
    void assignRoleToGroupSuccess() {
        when(groupRepository.findById("g1")).thenReturn(Optional.of(testGroup));
        when(roleRepository.findById("r1")).thenReturn(Optional.of(adminRole));

        roleDomainService.assignRoleToGroup("g1", "r1");

        assertTrue(testGroup.getRoles().contains(adminRole));
        verify(groupRepository).save(testGroup);
    }

    @Test
    @DisplayName("Should remove role from user successfully")
    void removeRoleFromUserSuccess() {
        testUser.assignDirectRoles(Set.of(adminRole));
        when(userRepository.findById("u1")).thenReturn(Optional.of(testUser));

        roleDomainService.removeRoleFromUser("u1", "role-1");

        assertTrue(testUser.getDirectRoles().isEmpty());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should remove role from group successfully")
    void removeRoleFromGroupSuccess() {
        testGroup.assignRoles(Set.of(adminRole));
        when(groupRepository.findById("g1")).thenReturn(Optional.of(testGroup));

        roleDomainService.removeRoleFromGroup("g1", "role-1");

        assertTrue(testGroup.getRoles().isEmpty());
        verify(groupRepository).save(testGroup);
    }
}
