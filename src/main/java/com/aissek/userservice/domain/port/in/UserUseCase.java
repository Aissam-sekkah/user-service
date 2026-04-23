package com.aissek.userservice.domain.port.in;

import com.aissek.userservice.domain.model.User;

import java.util.List;

/**
 * PORT ENTRANT : définit ce que le domaine EXPOSE vers l'extérieur.
 * Le controller REST dépend de cette interface, pas de l'implémentation
 */
public interface UserUseCase {

    User        createUser(String name, String email);
    User        getUserById(String id);
    List<User>  getAllUsers();
    User        updateUser(String id, String name, String email);
    void        deleteUser(String id);

}
