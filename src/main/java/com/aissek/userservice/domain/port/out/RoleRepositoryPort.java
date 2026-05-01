package com.aissek.userservice.domain.port.out;

import com.aissek.userservice.domain.model.Role;
import java.util.List;
import java.util.Optional;

public interface RoleRepositoryPort {
    Role save(Role role);
    Optional<Role> findById(String id);
    Optional<Role> findByName(String name);
    List<Role> findAll();
    void delete(String id);
}
