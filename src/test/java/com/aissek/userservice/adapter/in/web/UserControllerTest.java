package com.aissek.userservice.adapter.in.web;

import com.aissek.userservice.adapter.out.persistence.UserPersistenceAdapter;
import com.aissek.userservice.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.aissek.userservice.adapter.out.persistence.repository.UserJpaRepository;
import com.aissek.userservice.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PersistenceConfig.class)
class UserControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("userdb")
            .withUsername("postgres")
            .withPassword("secret");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private UserPersistenceAdapter adapter;

    @Autowired
    private UserPersistenceMapper mapper;

    @BeforeEach
    void setUp(){
        userJpaRepository.deleteAll();
        User exitingUser = new User("ali", "ali@gmail.com");
        adapter.save(exitingUser);
    }

    @Test
    void shouldPersistDomainUserToDatabase(){
        // Arrange
        User domainUser = new User("Ali", "eli@email.com");
        // Act
        User savedUser = adapter.save(domainUser);
        // Assert
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("eli@email.com");
      //  verify(userJpaRepository, times(1)).save(mapper.toEntity(domainUser));
    }

    @Test
    void shouldThrowEmailAlreadyExist(){
        // Arrange
        //User newUser = new User("Ali", "ali@email.com");
        User user2 = new User("amal", "ali@gmail.com");
        // Act
        // Assert
        assertThrows(DataIntegrityViolationException.class, () -> adapter.save(user2));
    }

}