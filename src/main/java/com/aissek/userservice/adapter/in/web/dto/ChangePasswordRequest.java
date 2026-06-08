package com.aissek.userservice.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload used to rotate a user's password.
 */
public record ChangePasswordRequest(
        @NotBlank(message = "currentPassword must not be blank")
        String currentPassword,
        @NotBlank(message = "newPassword must not be blank")
        @Size(min = 8, max = 72, message = "newPassword must be between 8 and 72 characters")
        String newPassword
) {
}
