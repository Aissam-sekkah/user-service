package com.aissek.userservice.domain.port.in;

import com.aissek.userservice.domain.model.Group;

import java.util.List;
import java.util.Optional;

public interface GroupUseCase {
    Group createGroup(String name, String description);
    Optional<Group> getGroup(String id);
    List<Group> getAllGroups();
    Group updateGroup(String id, String name, String description);
    void deleteGroup(String id);
}
