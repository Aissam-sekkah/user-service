package com.aissek.userservice.adapter.in.web.dto;

import java.time.LocalDateTime;

public record GroupResponse(String id, String name, String description, LocalDateTime createdAt) {}
