package com.aissek.userservice.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "name must not be blank")
        String name,
        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a well-formed email address")
        String email,
        @NotBlank(message = "password must not be blank")
        @Size(min = 8, message = "password must be at least 8 characters")
        String password
) {
}
