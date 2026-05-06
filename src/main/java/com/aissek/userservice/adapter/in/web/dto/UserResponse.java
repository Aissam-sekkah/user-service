package com.aissek.userservice.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record UserResponse(
        String id,
        String name,
        String email,
        LocalDateTime createdAt,
        Set<String> roles,
        List<GroupResponse> groups
) {
}
