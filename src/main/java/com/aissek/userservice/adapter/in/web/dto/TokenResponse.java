package com.aissek.userservice.adapter.in.web.dto;

/**
 * Response containing the authentication token.
 */
public record TokenResponse(
    String accessToken,
    String refreshToken,
    String type,
    long accessTokenExpiresIn,
    long refreshTokenExpiresIn
) {}
