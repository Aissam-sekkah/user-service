package com.aissek.userservice.domain.port.in;

import com.aissek.userservice.domain.model.Role;
import java.util.List;

public interface RoleUseCase {
    Role createRole(String name, String description);
    List<Role> getAllRoles();
    void assignRoleToUser(String userId, String roleId);
    void assignRoleToGroup(String groupId, String roleId);
    void removeRoleFromUser(String userId, String roleId);
    void removeRoleFromGroup(String groupId, String roleId);
}
