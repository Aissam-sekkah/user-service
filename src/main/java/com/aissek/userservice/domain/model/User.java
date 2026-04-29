package com.aissek.userservice.domain.model;

import com.aissek.userservice.domain.service.EmailValidator;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// Getters uniquement (immuabilité partielle)
@Getter
public class User {

    private final String id;
    private String name;
    private String email;
    private String passwordHash;
    private Set<Group> groups;
    private final LocalDateTime createdAt;



    // Constructeur de création (Génère l'ID)
    public User(String name, String email, String passwordHash, Set<Group> groups){
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.name = name;
        this.email = validateEmail(email);
        this.groups = (groups != null) ? groups : new HashSet<>();
        this.passwordHash = validatePasswordHash(passwordHash);
    }

    // Constructeur de reconstitution depuis la bdd
    public User(String id, String name, String email, String passwordHash,Set<Group> groups, LocalDateTime createdAt){
        this.id = id;
        this.name = name;
        this.email = validateEmail(email);
        this.passwordHash = validatePasswordHash(passwordHash);
        this.groups = groups;
        this.createdAt = createdAt;
    }

    // regle métier encapsuler
    public void updateProfile(String name, String email, Set<Group> groups){
        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("Le nom ne peut pas etre vide");
        }
        
        this.name = name;
        this.email = validateEmail(email);
        if(groups != null)
            this.groups = groups;
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = validatePasswordHash(passwordHash);
    }

    private String validateEmail(String email) {
        EmailValidator.validate(email);
        return email;
    }

    private String validatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Le mot de passe hashé ne peut pas etre vide");
        }
        return passwordHash;
    }

}
