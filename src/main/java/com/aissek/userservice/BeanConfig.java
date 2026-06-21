package com.aissek.userservice;


import com.aissek.userservice.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.aissek.userservice.adapter.out.persistence.repository.UserJpaRepository;
import com.aissek.userservice.adapter.out.security.JwtService;
import com.aissek.userservice.config.AuditConfig.AuditLogger;
import com.aissek.userservice.domain.model.PasswordPolicy;
import com.aissek.userservice.domain.model.Role;
import com.aissek.userservice.domain.model.User;
import com.aissek.userservice.domain.port.in.GroupUseCase;
import com.aissek.userservice.domain.port.in.UserUseCase;
import com.aissek.userservice.domain.port.out.GroupRepositoryPort;
import com.aissek.userservice.domain.port.out.PasswordHasherPort;
import com.aissek.userservice.domain.port.out.UserRepositoryPort;
import com.aissek.userservice.domain.service.GroupDomainService;
import com.aissek.userservice.domain.service.UserDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * On instencie le service doamine ICI, pas dans le domaine lui-meme,
 * Pour éviter les annotation Spring dans le coeur métier
 */

@Slf4j
@Configuration
public class BeanConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserUseCase UserUseCase(UserRepositoryPort userRepositoryPort, PasswordHasherPort passwordHasherPort, GroupRepositoryPort groupRepositoryPort, JwtService jwtService, AuditLogger auditLogger){
        return new UserDomainService(userRepositoryPort, passwordHasherPort, groupRepositoryPort, jwtService, auditLogger);
    }

    @Bean
    public GroupUseCase GroupUseCase(GroupRepositoryPort groupRepositoryPort){
        return new GroupDomainService(groupRepositoryPort);
    }

    @Bean
    public com.aissek.userservice.domain.port.in.RoleUseCase RoleUseCase(com.aissek.userservice.domain.port.out.RoleRepositoryPort roleRepositoryPort, UserRepositoryPort userRepositoryPort, GroupRepositoryPort groupRepositoryPort){
        return new com.aissek.userservice.domain.service.RoleDomainService(roleRepositoryPort, userRepositoryPort, groupRepositoryPort);
    }

    /**
     * Demo data — DEV ONLY. Seeds an admin and a staff account with well-known
     * credentials so the API can be explored locally. Never runs in prod/test.
     * Flyway runs automatically at startup (spring.flyway.enabled=true), so roles
     * referenced here already exist (see V6__seed_default_roles.sql).
     */
    @Bean
    @Profile("dev")
    CommandLineRunner demoDataSeeder(
            UserJpaRepository repository,
            PasswordHasherPort passwordHasher,
            UserPersistenceMapper mapper
    ) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(mapper.toEntity(
                        buildUser("Admin", "admin@example.com", passwordHasher.hash("Admin@12345"), "ROLE_ADMIN")));
                repository.save(mapper.toEntity(
                        buildUser("Staff", "staff@example.com", passwordHasher.hash("Staff@12345"), "ROLE_MANAGER")));
                log.info("[dev] Demo users seeded: admin@example.com (ADMIN), staff@example.com (MANAGER)");
            }
        };
    }

    /**
     * Production-safe admin bootstrap. Creates an initial ROLE_ADMIN account from
     * environment variables when set and absent, solving the chicken-and-egg of
     * admin-only endpoints. No secrets are committed: provide
     * APP_BOOTSTRAP_ADMIN_EMAIL / APP_BOOTSTRAP_ADMIN_PASSWORD at deploy time.
     */
    @Bean
    @Profile("!test")
    CommandLineRunner adminBootstrap(
            UserJpaRepository repository,
            PasswordHasherPort passwordHasher,
            UserPersistenceMapper mapper,
            @Value("${app.bootstrap.admin-email:}") String adminEmail,
            @Value("${app.bootstrap.admin-password:}") String adminPassword
    ) {
        return args -> {
            if (adminEmail.isBlank() || adminPassword.isBlank()) {
                return; // bootstrap not configured
            }
            if (repository.existsByEmail(adminEmail)) {
                log.info("Admin bootstrap skipped: {} already exists", adminEmail);
                return;
            }
            new PasswordPolicy(adminPassword); // enforce complexity on the bootstrap admin
            repository.save(mapper.toEntity(
                    buildUser("Administrator", adminEmail, passwordHasher.hash(adminPassword), "ROLE_ADMIN")));
            log.info("Bootstrap admin created: {}", adminEmail);
        };
    }

    private static User buildUser(String name, String email, String passwordHash, String roleId) {
        User user = new User(name, email, passwordHash, null);
        user.assignDirectRoles(Set.of(new Role(roleId, roleId, roleId + " role")));
        return user;
    }
}
