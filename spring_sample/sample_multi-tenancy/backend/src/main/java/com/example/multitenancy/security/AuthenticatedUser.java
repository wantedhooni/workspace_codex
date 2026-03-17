package com.example.multitenancy.security;

/**
 * 인증 이후 애플리케이션에서 사용하는 사용자 정보를 표현한다.
 */
public record AuthenticatedUser(
        String tenantId,
        String username,
        String displayName,
        String role
) {
}
