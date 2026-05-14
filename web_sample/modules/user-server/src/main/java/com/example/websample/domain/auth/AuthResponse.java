package com.example.websample.domain.auth;

import com.example.websample.domain.user.User;
import com.example.websample.domain.user.UserRole;

/** 인증 성공 시 클라이언트에 반환하는 토큰과 사용자 정보입니다. */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UserSummary user
) {
    public static AuthResponse of(String accessToken, String refreshToken, User user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", UserSummary.from(user));
    }

    public record UserSummary(Long id, String email, String name, UserRole role) {
        public static UserSummary from(User user) {
            return new UserSummary(user.getId(), user.getEmail(), user.getName(), user.getRole());
        }
    }
}
