package com.aissek.userservice.domain.service;

import com.aissek.userservice.domain.exception.*;
import com.aissek.userservice.domain.model.Group;
import com.aissek.userservice.domain.port.in.GroupUseCase;
import com.aissek.userservice.domain.port.out.GroupRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupDomainService implements GroupUseCase {

    private final GroupRepositoryPort groupRepositoryPort;

    @Override
    @Transactional
    public Group createGroup(String name, String description) {
        log.info("Attempting to create group: {}", name);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom du groupe ne peut pas être vide");
        }

        if (groupRepositoryPort.existsByName(name)) {
            log.warn("Group creation failed: group name {} already exists", name);
            throw new ConflictException("Le groupe avec le nom '" + name + "' existe déjà");
        }

        Group newGroup = new Group(name, description);
        Group savedGroup = groupRepositoryPort.save(newGroup);
        log.info("Group created successfully with ID: {}", savedGroup.getId());
        return savedGroup;
    }

    @Override
    public Optional<Group> getGroup(String id) {
        return groupRepositoryPort.findById(id);
    }

    @Override
    public List<Group> getAllGroups() {
        return groupRepositoryPort.findAll();
    }

    @Override
    @Transactional
    public Group updateGroup(String id, String name, String description) {
        log.info("Updating group ID: {}", id);
        return groupRepositoryPort.findById(id)
                .map(group -> {
                    group.updateGroup(name, description);
                    Group updated = groupRepositoryPort.save(group);
                    log.info("Group updated successfully: ID {}", updated.getId());
                    return updated;
                })
                .orElseThrow(() -> {
                    log.warn("Group update failed: ID {} not found", id);
                    return new ResourceNotFoundException("Groupe non trouvé : " + id);
                });
    }

    @Override
    @Transactional
    public void deleteGroup(String id) {
        log.info("Deleting group ID: {}", id);
        if (groupRepositoryPort.findById(id).isEmpty()) {
            log.warn("Group deletion failed: ID {} not found", id);
            throw new ResourceNotFoundException("Groupe non trouvé : " + id);
        }
        groupRepositoryPort.deleteById(id);
        log.info("Group deleted successfully: ID {}", id);
    }
}
