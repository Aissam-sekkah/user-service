package com.aissek.userservice.adapter.in.web.dto;

import java.time.LocalDateTime;

public record UserResponse(
        String id,
        String name,
        String email,
        LocalDateTime createdAt
) {
}
