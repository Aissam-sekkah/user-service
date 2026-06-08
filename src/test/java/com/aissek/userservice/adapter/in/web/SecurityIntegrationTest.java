package com.aissek.userservice.adapter.in.web;

import com.aissek.userservice.adapter.out.security.JwtService;
import com.aissek.userservice.domain.model.User;
import com.aissek.userservice.domain.port.in.UserUseCase;
import com.aissek.userservice.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UserUseCase userUseCase;

    @MockBean
    private UserRepositoryPort userRepositoryPort;

    @Test
    @DisplayName("Public endpoint /api/v1/auth/login should return access and refresh tokens")
    void loginEndpointShouldReturnBothTokens() throws Exception {
        String loginJson = "{\"email\":\"test@example.com\",\"password\":\"Password123!\"}";
        
        User dummyUser = new User("Test", "test@example.com", "hashed_pw", Set.of());
        when(userUseCase.login(anyString(), anyString())).thenReturn(dummyUser);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertTrue(content.contains("accessToken"));
                    assertTrue(content.contains("refreshToken"));
                });
    }

    @Test
    @DisplayName("Refresh endpoint should return new tokens for valid refresh token")
    void refreshEndpointShouldReturnNewTokens() throws Exception {
        User dummyUser = new User("Test", "test@example.com", "hashed_pw", Set.of());
        String validRefreshToken = jwtService.generateRefreshToken("test@example.com");
        
        // We must mock the hashedPassword token for the dummyUser
        // In a real scenario, it's hashed by PasswordHasherPort
        // But since we are mocking UserUseCase.refreshAccessToken, 
        // the internal logic of UserDomainService (which does the hashing) 
        // is bypassed if we mock the use case.
        when(userUseCase.refreshAccessToken(validRefreshToken)).thenReturn(dummyUser);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + validRefreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertTrue(content.contains("accessToken"));
                    assertTrue(content.contains("refreshToken"));
                });
    }

    @Test
    @DisplayName("Refresh endpoint should return 401 for invalid refresh token")
    void refreshEndpointShouldReturn401ForInvalidToken() throws Exception {
        String invalidToken = "invalid-token";
        when(userUseCase.refreshAccessToken(invalidToken)).thenThrow(new com.aissek.userservice.domain.exception.AuthenticationException("Invalid or expired refresh token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + invalidToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint should return 401 when no token is provided")
    void protectedEndpointShouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint should return 200 when valid token is provided")
    void protectedEndpointShouldReturn200WithValidToken() throws Exception {
        String email = "test@example.com";
        String token = jwtService.generateToken(email);
        
        User dummyUser = new User("Test", email, "hashed_pw", Set.of());
        // GET /api/v1/users (listing) requires MANAGER+; a plain USER is intentionally denied.
        dummyUser.assignDirectRoles(Set.of(new com.aissek.userservice.domain.model.Role("role-manager", "ROLE_MANAGER", "Manager Role")));
        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.of(dummyUser));
        
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected endpoint should return 401 when invalid token is provided")
    void protectedEndpointShouldReturn401WithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer invalid-token-here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Swagger UI should be accessible without token")
    void swaggerShouldBePublic() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("A plain USER can change their OWN password")
    void userCanChangeOwnPassword() throws Exception {
        String email = "owner@example.com";
        String token = jwtService.generateToken(email);

        User owner = new User("Owner", email, "hashed_pw", Set.of());
        owner.assignDirectRoles(Set.of(new com.aissek.userservice.domain.model.Role("role-user", "ROLE_USER", "Standard User Role")));
        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.of(owner));

        mockMvc.perform(put("/api/v1/users/" + owner.getId() + "/password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"Password123!\",\"newPassword\":\"NewPassword123!\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("A plain USER cannot change ANOTHER user's password")
    void userCannotChangeOthersPassword() throws Exception {
        String email = "owner@example.com";
        String token = jwtService.generateToken(email);

        User owner = new User("Owner", email, "hashed_pw", Set.of());
        owner.assignDirectRoles(Set.of(new com.aissek.userservice.domain.model.Role("role-user", "ROLE_USER", "Standard User Role")));
        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.of(owner));

        mockMvc.perform(put("/api/v1/users/some-other-user-id/password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"Password123!\",\"newPassword\":\"NewPassword123!\"}"))
                .andExpect(status().isForbidden());
    }
}
