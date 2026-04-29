package com.aissek.userservice.domain.service;

import com.aissek.userservice.domain.model.Group;
import com.aissek.userservice.domain.port.in.GroupUseCase;
import com.aissek.userservice.domain.port.out.GroupRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class GroupDomainService implements GroupUseCase {

    private final GroupRepositoryPort groupRepositoryPort;

    @Override
    public Group createGroup(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom du groupe ne peut pas être vide");
        }

        if (groupRepositoryPort.existsByName(name)) {
            throw new GroupAlreadyExistsException("Le groupe avec le nom '" + name + "' existe déjà");
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
    public Group updateGroup(String id, String name, String description) {
        return groupRepositoryPort.findById(id)
                .map(group -> {
                    group.updateGroup(name, description);
                    return groupRepositoryPort.save(group);
                })
                .orElseThrow(() -> new IllegalArgumentException("Groupe non trouvé : " + id));
    }

    @Override
    public void deleteGroup(String id) {
        if (groupRepositoryPort.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Groupe non trouvé : " + id);
        }
        groupRepositoryPort.deleteById(id);
    }

    public static class GroupAlreadyExistsException extends RuntimeException {
        public GroupAlreadyExistsException(String message) {
            super(message);
        }
    }
}
