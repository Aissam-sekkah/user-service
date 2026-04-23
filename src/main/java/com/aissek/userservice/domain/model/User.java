package com.aissek.userservice.domain.model;


import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

// Getters uniquement (immuabilité partielle)
@Getter
public class User {

    private final String id;
    private String name;
    private String email;
    private String passwordHash;
    private final LocalDateTime createdAt;

    // Constructeur de création (Génère l'ID)
    public User(String name, String email, String passwordHash){
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.name = name;
        this.email = email;
        this.passwordHash = validatePasswordHash(passwordHash);
    }

    // Constructeur de reconstitution depuis la bdd
    public User(String id, String name, String email, String passwordHash, LocalDateTime createdAt){
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = validatePasswordHash(passwordHash);
        this.createdAt = createdAt;
    }

    // regle métier encapsuler
    public void updateProfile(String name, String email){
        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("Le nom ne peut pas etre vide");
        }
        if(!email.contains("@")){
            throw new IllegalArgumentException("Email invalide");
        }
        this.name = name;
        this.email = email;
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = validatePasswordHash(passwordHash);
    }

    private String validatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Le mot de passe hashé ne peut pas etre vide");
        }
        return passwordHash;
    }

}
