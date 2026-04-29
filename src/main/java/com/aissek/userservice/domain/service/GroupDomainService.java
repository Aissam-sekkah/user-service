package com.aissek.userservice.domain.service;

import com.aissek.userservice.domain.exception.*;
import com.aissek.userservice.domain.model.Group;
import com.aissek.userservice.domain.port.in.GroupUseCase;
import com.aissek.userservice.domain.port.out.GroupRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupDomainService implements GroupUseCase {

    private final GroupRepositoryPort groupRepositoryPort;

    @Override
    @Transactional
    public Group createGroup(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom du groupe ne peut pas être vide");
        }

        if (groupRepositoryPort.existsByName(name)) {
            throw new ConflictException("Le groupe avec le nom '" + name + "' existe déjà");
        }

        Group newGroup = new Group(name, description);
        return groupRepositoryPort.save(newGroup);
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
        return groupRepositoryPort.findById(id)
                .map(group -> {
                    group.updateGroup(name, description);
                    return groupRepositoryPort.save(group);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Groupe non trouvé : " + id));
    }

    @Override
    @Transactional
    public void deleteGroup(String id) {
        if (groupRepositoryPort.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Groupe non trouvé : " + id);
        }
        groupRepositoryPort.deleteById(id);
    }
}
