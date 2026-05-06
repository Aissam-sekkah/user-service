package com.aissek.userservice.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Group {

    private final String id;
    private String name;
    private String description;
    private java.util.Set<Role> roles;
    private final LocalDateTime createdAt;

    // Constructeur de création (Génère l'ID)
    public Group(String name, String description){
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.roles = new java.util.HashSet<>();
        this.createdAt = LocalDateTime.now();
    }

    // Constructeur de reconstitution depuis la bdd
    public Group(String id, String name, String description, java.util.Set<Role> roles, LocalDateTime createdAt){
        this.id = id;
        this.name = name;
        this.description = description;
        this.roles = roles;
        this.createdAt = createdAt;
    }

    public void assignRoles(java.util.Set<Role> roles) {
        this.roles = roles;
    }

    // regle métier encapsuler
    public void updateGroup(String name, String description){
        if(name == null || name.isBlank())
            throw new IllegalArgumentException("Le nom ne peut pas etre vide");

        this.name = name;
        this.description = description;
    }
}
