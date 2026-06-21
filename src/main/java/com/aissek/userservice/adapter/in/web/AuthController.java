package com.aissek.userservice.adapter.in.web;

import com.aissek.userservice.adapter.in.web.dto.CreateUserRequest;
import com.aissek.userservice.adapter.in.web.dto.LoginRequest;
import com.aissek.userservice.adapter.in.web.dto.TokenResponse;
import com.aissek.userservice.adapter.in.web.dto.RefreshRequest;
import com.aissek.userservice.adapter.out.security.UserSecurityDetails;
import com.aissek.userservice.domain.model.Role;
import com.aissek.userservice.domain.port.in.UserUseCase;
import com.aissek.userservice.domain.port.out.TokenServicePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * HTTP entry point for authentication operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserUseCase userUseCase;
    private final TokenServicePort jwtService;

    @Value("${application.security.jwt.expiration:3600000}")
    private long accessTokenExpiration;

    @Value("${application.security.jwt.refresh-expiration:604800000}")
    private long refreshTokenExpiration;

    /**
     * Public registration. 
     * Security: Public users are ALWAYS forced to ROLE_USER regardless of request.
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody CreateUserRequest request) {
        log.info("Public registration attempt for email: {}", request.email());
        
        // SECURITY GUARD: Public registrants cannot pick their own roles.
        Set<Role> defaultRoles = Set.of(new Role("ROLE_USER", "ROLE_USER", "Standard User Role"));
        
        userUseCase.createUser(request.name(), request.email(), request.password(), null, defaultRoles);
        
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Authenticates a user and returns access and refresh tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());
        var user = userUseCase.login(request.email(), request.password());
        
        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        
        userUseCase.updateRefreshToken(user.getId(), refreshToken);
        
        log.info("Login successful for user: {}", user.getEmail());
        return ResponseEntity.ok(new TokenResponse(
                accessToken, 
                refreshToken, 
                "Bearer", 
                accessTokenExpiration,
                refreshTokenExpiration
        ));
    }

    /**
     * Refreshes an access token using a valid refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        log.info("Token refresh attempt");
        var user = userUseCase.refreshAccessToken(request.refreshToken());
        
        String newAccessToken = jwtService.generateToken(user.getEmail());
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());
        
        userUseCase.updateRefreshToken(user.getId(), newRefreshToken);
        
        log.info("Token refresh successful for user: {}", user.getEmail());
        return ResponseEntity.ok(new TokenResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                accessTokenExpiration,
                refreshTokenExpiration
        ));
    }

    /**
     * Revokes the caller's refresh token (server-side logout).
     * Requires a valid access token; the JWT filter populates the principal even
     * on this permit-all path.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserSecurityDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Logout request for user: {}", principal.getUsername());
        userUseCase.logout(principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
