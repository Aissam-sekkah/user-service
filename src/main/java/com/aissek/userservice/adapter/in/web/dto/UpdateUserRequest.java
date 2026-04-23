package com.aissek.userservice.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload used to update user profile data.
 */
public record UpdateUserRequest(
        @NotBlank(message = "name must not be blank")
        String name,
        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a well-formed email address")
        String email
) {
}
