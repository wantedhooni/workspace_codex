package com.example.observability.security;

/**
 * JWT에서 추출한 사용자 식별 정보다.
 */
public record AuthenticatedUser(
        String tenantId,
        String username,
        String displayName,
        String role
) {
}
