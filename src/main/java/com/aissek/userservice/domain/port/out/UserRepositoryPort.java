package com.aissek.userservice.domain.port.out;

import com.aissek.userservice.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * PORT SORTANT : définit ce dont le domaine a BESOIN de l'éxtérieur.
 * Le domaine ne connait pas JPA, il parle uniquement a cette interface
 */
public interface UserRepositoryPort {

    User            save(User user);
    Optional<User>  findById(String id);
    List<User>      findAll();
    boolean         existByEmail(String email);
    void            deleteById(String id);

}
