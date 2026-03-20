package com.example.observability.web.dto;

import java.time.Instant;

public record LoginResponse(
        String accessToken,
        Instant expiresAt,
        UserProfile user
) {
    public record UserProfile(
            String tenantId,
            String username,
            String displayName,
            String role
    ) {
    }
}
