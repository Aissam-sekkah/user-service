package com.aissek.userservice;


import com.aissek.userservice.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.aissek.userservice.adapter.out.persistence.repository.UserJpaRepository;
import com.aissek.userservice.domain.model.User;
import com.aissek.userservice.domain.port.in.GroupUseCase;
import com.aissek.userservice.domain.port.in.UserUseCase;
import com.aissek.userservice.domain.port.out.GroupRepositoryPort;
import com.aissek.userservice.domain.port.out.PasswordHasherPort;
import com.aissek.userservice.domain.port.out.UserRepositoryPort;
import com.aissek.userservice.domain.service.GroupDomainService;
import com.aissek.userservice.domain.service.UserDomainService;
import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * On instencie le service doamine ICI, pas dans le domaine lui-meme,
 * Pour éviter les annotation Spring dans le coeur métier
 */

@Configuration
public class BeanConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserUseCase UserUseCase(UserRepositoryPort userRepositoryPort, PasswordHasherPort passwordHasherPort, GroupRepositoryPort groupRepositoryPort){
        return new UserDomainService(userRepositoryPort, passwordHasherPort, groupRepositoryPort);
    }

    @Bean
    public GroupUseCase GroupUseCase(GroupRepositoryPort groupRepositoryPort){
        return new GroupDomainService(groupRepositoryPort);
    }

    @Bean
    @org.springframework.context.annotation.Profile("!test")
    CommandLineRunner initDatabase(
            UserJpaRepository repository,
            PasswordHasherPort passwordHasherPort,
            ObjectProvider<Flyway> flywayProvider
    ) {
        UserPersistenceMapper mapper = new UserPersistenceMapper();
        return args -> {
            flywayProvider.ifAvailable(Flyway::migrate);
            if (repository.count() == 0) {
                repository.save(mapper.toEntity(new User("Admin", "admin@example.com", passwordHasherPort.hash("admin12345"), null)));
                repository.save(mapper.toEntity(new User("Staff", "staff@example.com", passwordHasherPort.hash("staff12345"), null)));
                System.out.println("Database pre-populated!");
            }
        };
    }
}
