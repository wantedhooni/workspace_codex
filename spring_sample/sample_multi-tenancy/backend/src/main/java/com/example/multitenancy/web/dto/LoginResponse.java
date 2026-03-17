package com.example.multitenancy.web.dto;

import java.time.Instant;

/**
 * 로그인 성공 시 액세스 토큰과 사용자 정보를 반환한다.
 */
public record LoginResponse(
        String accessToken,
        Instant expiresAt,
        UserProfile user
) {

    /**
     * 프론트엔드 표시에 필요한 최소 사용자 프로필이다.
     */
    public record UserProfile(
            String tenantId,
            String username,
            String displayName,
            String role
    ) {
    }
}
