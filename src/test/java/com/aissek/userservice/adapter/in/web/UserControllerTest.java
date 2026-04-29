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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.containsString;
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
@ActiveProfiles("test")
class UserControllerTest {

    private static final String BASE_URL = "/api/v1/users";
    private static final String AUTH_BASE_URL = "/api/v1/auth";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(false);

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

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User existingUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userJpaRepository.deleteAll();
        existingUser = adapter.save(new User("ali", "ali@gmail.com", passwordEncoder.encode("password123"), null));
    }

    @Test
    void shouldCreateUser() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Amal","email":"amal@gmail.com","password":"password123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Amal"))
                .andExpect(jsonPath("$.email").value("amal@gmail.com"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        assertThat(userJpaRepository.existsByEmail("amal@gmail.com")).isTrue();
        var savedEntity = userJpaRepository.findAll().stream()
                .filter(user -> user.getEmail().equals("amal@gmail.com"))
                .findFirst()
                .orElseThrow();
        assertThat(savedEntity.getPasswordHash()).isNotBlank();
        assertThat(savedEntity.getPasswordHash()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", savedEntity.getPasswordHash())).isTrue();
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        mockMvc.perform(post(AUTH_BASE_URL + "/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"ali@gmail.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingUser.getId()))
                .andExpect(jsonPath("$.name").value("ali"))
                .andExpect(jsonPath("$.email").value("ali@gmail.com"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginPasswordIsInvalid() throws Exception {
        mockMvc.perform(post(AUTH_BASE_URL + "/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"ali@gmail.com","password":"wrongPassword"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Email ou mot de passe invalide"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginEmailDoesNotExist() throws Exception {
        mockMvc.perform(post(AUTH_BASE_URL + "/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"missing@gmail.com","password":"password123"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Email ou mot de passe invalide"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldReturnBadRequestWhenLoginEmailIsInvalid() throws Exception {
        mockMvc.perform(post(AUTH_BASE_URL + "/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"invalid-email","password":"password123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("email: email must be a well-formed email address")))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenLoginPasswordIsBlank() throws Exception {
        mockMvc.perform(post(AUTH_BASE_URL + "/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"ali@gmail.com","password":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("password: password must not be blank")))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnUserById() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + existingUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingUser.getId()))
                .andExpect(jsonPath("$.name").value("ali"))
                .andExpect(jsonPath("$.email").value("ali@gmail.com"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void shouldReturnAllUsers() throws Exception {
        adapter.save(new User("Sara", "sara@gmail.com", passwordEncoder.encode("password456"), null));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].email").isArray())
                .andExpect(jsonPath("$[*].createdAt").isArray())
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
                .andExpect(jsonPath("$.email").value("ali.updated@gmail.com"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
        assertThat(userJpaRepository.existsByEmail("ali.updated@gmail.com")).isTrue();
    }

    @Test
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + existingUser.getId()))
                .andExpect(status().isNoContent());
        assertThat(userJpaRepository.findById(existingUser.getId())).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUserDoesNotExist() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found : missing-id"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Amal","email":"ali@gmail.com","password":"password123"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Email déjà utilisé : ali@gmail.com"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingWithInvalidEmail() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Ali","email":"invalid-email","password":"password123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("email: email must be a well-formed email address")))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingWithBlankName() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"   ","email":"ali@example.com","password":"password123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("name: name must not be blank")))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingWithNullEmail() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Ali","email":null,"password":"password123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("email: email must not be blank")))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingWithBlankPassword() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Ali","email":"ali@example.com","password":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("password: password must not be blank")))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingWithShortPassword() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Ali","email":"ali@example.com","password":"short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("password: password must be at least 8 characters")))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingWithInvalidEmail() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + existingUser.getId())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Ali","email":"invalid-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("email: email must be a well-formed email address")))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingWithBlankName() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + existingUser.getId())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"   ","email":"ali.updated@gmail.com"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("name: name must not be blank")))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingWithNullName() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + existingUser.getId())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":null,"email":"ali.updated@gmail.com"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("name: name must not be blank")))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUserDoesNotExist() throws Exception {
        mockMvc.perform(put(BASE_URL + "/missing-id")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Ali Updated","email":"ali.updated@gmail.com"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found : missing-id"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldChangePassword() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + existingUser.getId() + "/password")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"password123","newPassword":"newPassword123"}
                                """))
                .andExpect(status().isNoContent());

        var savedEntity = userJpaRepository.findById(existingUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("newPassword123", savedEntity.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("password123", savedEntity.getPasswordHash())).isFalse();
    }

    @Test
    void shouldLoginWithNewPasswordOnlyAfterPasswordChange() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + existingUser.getId() + "/password")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"password123","newPassword":"newPassword123"}
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(post(AUTH_BASE_URL + "/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"ali@gmail.com","password":"password123"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Email ou mot de passe invalide"))
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(post(AUTH_BASE_URL + "/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"ali@gmail.com","password":"newPassword123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingUser.getId()))
                .andExpect(jsonPath("$.email").value("ali@gmail.com"));
    }

    @Test
    void shouldReturnBadRequestWhenCurrentPasswordIsInvalid() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + existingUser.getId() + "/password")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"wrongPassword","newPassword":"newPassword123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Mot de passe actuel invalide"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenNewPasswordIsTooShort() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + existingUser.getId() + "/password")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"password123","newPassword":"short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("newPassword: newPassword must be at least 8 characters")))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenCurrentPasswordIsBlank() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + existingUser.getId() + "/password")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"   ","newPassword":"newPassword123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("currentPassword: currentPassword must not be blank")))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnNotFoundWhenChangingPasswordForUnknownUser() throws Exception {
        mockMvc.perform(put(BASE_URL + "/missing-id/password")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"password123","newPassword":"newPassword123"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found : missing-id"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        mockMvc.perform(get(BASE_URL + "/missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found : missing-id"))
                .andExpect(jsonPath("$.status").value(404));
    }
}
