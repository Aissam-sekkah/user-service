package com.aissek.userservice.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        String id,
        String name,
        String email,
        LocalDateTime createdAt,
        List<GroupResponse> groups
) {
}
