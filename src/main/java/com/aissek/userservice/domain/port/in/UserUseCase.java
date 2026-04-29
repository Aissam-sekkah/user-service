package com.aissek.userservice.domain.port.in;

import com.aissek.userservice.domain.model.Group;
import com.aissek.userservice.domain.model.User;

import java.util.List;
import java.util.Set;

/**
 * PORT ENTRANT : définit ce que le domaine EXPOSE vers l'extérieur.
 * Le controller REST dépend de cette interface, pas de l'implémentation
 */
public interface UserUseCase {

    User        createUser(String name, String email, String password, Set<Group> groups);
    User        getUserById(String id);
    User        login(String email, String password);
    List<User>  getAllUsers();
    User        updateUser(String id, String name, String email, Set<Group> groups);
    void        changePassword(String id, String currentPassword, String newPassword);
    void        deleteUser(String id);

}
