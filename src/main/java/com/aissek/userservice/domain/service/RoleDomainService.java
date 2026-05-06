package com.aissek.userservice.domain.service;

import com.aissek.userservice.domain.exception.ConflictException;
import com.aissek.userservice.domain.exception.ResourceNotFoundException;
import com.aissek.userservice.domain.model.Group;
import com.aissek.userservice.domain.model.Role;
import com.aissek.userservice.domain.model.User;
import com.aissek.userservice.domain.port.in.RoleUseCase;
import com.aissek.userservice.domain.port.out.GroupRepositoryPort;
import com.aissek.userservice.domain.port.out.RoleRepositoryPort;
import com.aissek.userservice.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Transactional
public class RoleDomainService implements RoleUseCase {

    private final RoleRepositoryPort roleRepository;
    private final UserRepositoryPort userRepository;
    private final GroupRepositoryPort groupRepository;

    @Override
    public Role createRole(String name, String description) {
        log.info("Creating new role: {}", name);
        if (roleRepository.findByName(name).isPresent()) {
            throw new ConflictException("Role already exists: " + name);
        }
        
        Role role = new Role(UUID.randomUUID().toString(), name, description);
        return roleRepository.save(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleById(String id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
    }

    @Override
    public void assignRoleToUser(String userId, String roleId) {
        log.info("Assigning role {} to user {}", roleId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));

        Set<Role> roles = new HashSet<>(user.getDirectRoles());
        roles.add(role);
        user.assignDirectRoles(roles);
        userRepository.save(user);
    }

    @Override
    public void assignRoleToGroup(String groupId, String roleId) {
        log.info("Assigning role {} to group {}", roleId, groupId);
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));

        Set<Role> roles = new HashSet<>(group.getRoles());
        roles.add(role);
        group.assignRoles(roles);
        groupRepository.save(group);
    }

    @Override
    public void removeRoleFromUser(String userId, String roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        
        Set<Role> roles = new HashSet<>(user.getDirectRoles());
        roles.removeIf(r -> r.getId().equals(roleId));
        user.assignDirectRoles(roles);
        userRepository.save(user);
    }

    @Override
    public void removeRoleFromGroup(String groupId, String roleId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId));
        
        Set<Role> roles = new HashSet<>(group.getRoles());
        roles.removeIf(r -> r.getId().equals(roleId));
        group.assignRoles(roles);
        groupRepository.save(group);
    }
}
