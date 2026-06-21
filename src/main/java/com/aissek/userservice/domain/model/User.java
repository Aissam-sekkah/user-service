package com.aissek.userservice.domain.model;

import com.aissek.userservice.domain.service.EmailValidator;
import lombok.Getter;

import java.time.Duration;
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
    private String refreshToken;
    private Set<Group> groups;
    private Set<Role> directRoles;
    private int failedLoginAttempts;
    private LocalDateTime lockedUntil;
    private final LocalDateTime createdAt;



    // Constructeur de création (Génère l'ID)
    public User(String name, String email, String passwordHash, Set<Group> groups){
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.name = name;
        this.email = validateEmail(email);
        this.groups = (groups != null) ? groups : new HashSet<>();
        this.directRoles = new HashSet<>();
        this.passwordHash = validatePasswordHash(passwordHash);
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    // Constructeur de reconstitution depuis la bdd
    public User(String id, String name, String email, String passwordHash, String refreshToken, Set<Group> groups, Set<Role> directRoles, LocalDateTime createdAt){
        this(id, name, email, passwordHash, refreshToken, groups, directRoles, 0, null, createdAt);
    }

    // Constructeur de reconstitution complet (avec état de verrouillage)
    public User(String id, String name, String email, String passwordHash, String refreshToken, Set<Group> groups, Set<Role> directRoles, int failedLoginAttempts, LocalDateTime lockedUntil, LocalDateTime createdAt){
        this.id = id;
        this.name = name;
        this.email = validateEmail(email);
        this.passwordHash = validatePasswordHash(passwordHash);
        this.refreshToken = refreshToken;
        this.groups = groups;
        this.directRoles = directRoles;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil = lockedUntil;
        this.createdAt = createdAt;
    }

    /**
     * Calculates the union of roles assigned directly to the user 
     * and roles inherited from their group memberships.
     */
    public Set<Role> getEffectiveRoles() {
        Set<Role> effective = new HashSet<>(this.directRoles != null ? this.directRoles : java.util.Collections.emptySet());
        if (this.groups != null) {
            this.groups.forEach(group -> {
                if (group.getRoles() != null) {
                    effective.addAll(group.getRoles());
                }
            });
        }
        return effective;
    }

    public void assignDirectRoles(Set<Role> roles) {
        // Defensive copy so external mutation of the passed set can't leak into the aggregate.
        this.directRoles = (roles != null) ? new HashSet<>(roles) : new HashSet<>();
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

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * @return true while the account is temporarily locked out from authenticating.
     */
    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * Records a failed authentication attempt; locks the account for {@code lockDuration}
     * once {@code maxAttempts} consecutive failures are reached.
     */
    public void recordFailedLogin(int maxAttempts, Duration lockDuration) {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= maxAttempts) {
            this.lockedUntil = LocalDateTime.now().plus(lockDuration);
        }
    }

    /**
     * Clears the failed-attempt counter and any active lock (call on successful login).
     */
    public void resetFailedLogins() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
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
