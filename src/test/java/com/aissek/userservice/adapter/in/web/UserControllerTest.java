package com.aissek.userservice.adapter.in.web;

import com.aissek.userservice.adapter.out.persistence.UserPersistenceAdapter;
import com.aissek.userservice.adapter.out.persistence.repository.UserJpaRepository;
import com.aissek.userservice.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(PersistenceConfig.class)
class UserControllerTest {

    private static final String BASE_URL = "/api/v1/users";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("userdb")
            .withUsername("postgres")
            .withPassword("secret");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private UserPersistenceAdapter adapter;

    private User existingUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userJpaRepository.deleteAll();
        existingUser = adapter.save(new User("ali", "ali@gmail.com"));
    }

    @Test
    void shouldCreateUser() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Amal","email":"amal@gmail.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Amal"))
                .andExpect(jsonPath("$.email").value("amal@gmail.com"));
        assertThat(userJpaRepository.existsByEmail("amal@gmail.com")).isTrue();
    }

    @Test
    void shouldReturnUserById() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + existingUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingUser.getId()))
                .andExpect(jsonPath("$.name").value("ali"))
                .andExpect(jsonPath("$.email").value("ali@gmail.com"));
    }

    @Test
    void shouldReturnAllUsers() throws Exception {
        adapter.save(new User("Sara", "sara@gmail.com"));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].email").isArray())
                .andExpect(jsonPath("$[*].email").value(org.hamcrest.Matchers.containsInAnyOrder("ali@gmail.com", "sara@gmail.com")));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + existingUser.getId())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Ali Updated","email":"ali.updated@gmail.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingUser.getId()))
                .andExpect(jsonPath("$.name").value("Ali Updated"))
                .andExpect(jsonPath("$.email").value("ali.updated@gmail.com"));
        assertThat(userJpaRepository.existsByEmail("ali.updated@gmail.com")).isTrue();
    }

    @Test
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + existingUser.getId()))
                .andExpect(status().isNoContent());
        assertThat(userJpaRepository.findById(existingUser.getId())).isEmpty();
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Amal","email":"ali@gmail.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Email déjà utilisé : ali@gmail.com"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingWithInvalidEmail() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + existingUser.getId())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Ali","email":"invalid-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Email invalide"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        mockMvc.perform(get(BASE_URL + "/missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found : missing-id"))
                .andExpect(jsonPath("$.status").value(404));
    }
}
