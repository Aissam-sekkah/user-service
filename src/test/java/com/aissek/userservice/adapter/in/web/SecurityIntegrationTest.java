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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        when(userUseCase.refreshAccessToken("valid-refresh-token")).thenReturn(dummyUser);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("valid-refresh-token"))
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
        when(userUseCase.refreshAccessToken("invalid-token"))
                .thenThrow(new com.aissek.userservice.domain.exception.AuthenticationException("Invalid token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid-token"))
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
}
