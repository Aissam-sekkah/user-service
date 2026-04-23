package com.aissek.userservice.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload used to authenticate a user.
 */
public record LoginRequest(
        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a well-formed email address")
        String email,
        @NotBlank(message = "password must not be blank")
        String password
) {
}
