package com.aissek.userservice.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @NotBlank(message = "name must not be blank")
        String name,
        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a well-formed email address")
        String email
) {
}
