package com.aissek.userservice;


import com.aissek.userservice.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.aissek.userservice.adapter.out.persistence.repository.UserJpaRepository;
import com.aissek.userservice.domain.model.User;
import com.aissek.userservice.domain.port.out.UserRepositoryPort;
import com.aissek.userservice.domain.service.UserDomainService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * On instencie le service doamine ICI, pas dans le domaine lui-meme,
 * Pour éviter les annotation Spring dans le coeur métier
 */

@Configuration
public class BeanConfig {

    @Bean
    public UserDomainService userDomainService(UserRepositoryPort userRepositoryPort){
        return new UserDomainService(userRepositoryPort);
    }

    @Bean
    CommandLineRunner initDatabase(UserJpaRepository repository) {
        UserPersistenceMapper mapper = new UserPersistenceMapper();
        return args -> {
            if (repository.count() == 0) {
                repository.save(mapper.toEntity(new User("Admin", "admin@example.com" )));
                repository.save(mapper.toEntity(new User("Staff", "staff@example.com")));
                System.out.println("Database pre-populated!");
            }
        };
    }
}
