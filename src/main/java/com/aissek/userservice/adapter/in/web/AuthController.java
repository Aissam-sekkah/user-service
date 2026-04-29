package com.aissek.userservice.adapter.in.web;

import com.aissek.userservice.adapter.in.web.dto.LoginRequest;
import com.aissek.userservice.adapter.in.web.dto.UserResponse;
import com.aissek.userservice.adapter.in.web.mapper.UserWebMapper;
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
    private final UserWebMapper mapper;

    /**
     * Authenticates a user with email and password.
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        var user = userUseCase.login(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.OK).body(mapper.toResponse(user));
    }
}
