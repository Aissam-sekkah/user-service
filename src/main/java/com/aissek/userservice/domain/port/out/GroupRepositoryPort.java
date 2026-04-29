package com.aissek.userservice.domain.port.out;

import com.aissek.userservice.domain.model.Group;

import java.util.List;
import java.util.Optional;

public interface GroupRepositoryPort {
    Optional<Group> findById(String id);
    List<Group> findAll();
    Group save(Group group);
    void deleteById(String id);
    boolean existsByName(String name);
}
