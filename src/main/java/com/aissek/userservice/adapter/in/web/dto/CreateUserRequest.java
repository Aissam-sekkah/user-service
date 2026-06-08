package com.aissek.userservice.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Request payload used to create a user with credentials.
 */
public record CreateUserRequest(
        @NotBlank(message = "name must not be blank")
        String name,
        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a well-formed email address")
        String email,
        @NotBlank(message = "password must not be blank")
        @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
        String password,
        Set<String> groupIds,
        Set<String> roleIds
) {
}
