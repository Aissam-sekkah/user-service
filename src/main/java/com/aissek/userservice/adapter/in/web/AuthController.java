package com.aissek.userservice.adapter.in.web;

import com.aissek.userservice.adapter.in.web.dto.LoginRequest;
import com.aissek.userservice.adapter.in.web.dto.TokenResponse;
import com.aissek.userservice.adapter.in.web.dto.RefreshRequest;
import com.aissek.userservice.adapter.out.security.JwtService;
import com.aissek.userservice.domain.port.in.UserUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * HTTP entry point for authentication operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserUseCase userUseCase;
    private final JwtService jwtService;

    /**
     * Authenticates a user and returns access and refresh tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        var user = userUseCase.login(request.email(), request.password());
        
        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        
        userUseCase.updateRefreshToken(user.getId(), refreshToken);
        
        return ResponseEntity.ok(new TokenResponse(
                accessToken, 
                refreshToken, 
                "Bearer", 
                3600000L, // 1 hour
                604800000L // 7 days
        ));
    }

    /**
     * Refreshes an access token using a valid refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        var user = userUseCase.refreshAccessToken(request.refreshToken());
        
        String newAccessToken = jwtService.generateToken(user.getEmail());
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());
        
        userUseCase.updateRefreshToken(user.getId(), newRefreshToken);
        
        return ResponseEntity.ok(new TokenResponse(
                newAccessToken, 
                newRefreshToken, 
                "Bearer", 
                3600000L, 
                604800000L
        ));
    }
}
