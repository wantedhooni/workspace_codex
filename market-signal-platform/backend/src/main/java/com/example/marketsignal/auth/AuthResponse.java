package com.example.marketsignal.auth;

import com.example.marketsignal.user.UserProfileResponse;

/**
 * 인증 성공 시 반환하는 토큰 응답 모델이다.
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        UserProfileResponse user
) {

    public static AuthResponse of(String accessToken, UserProfileResponse user) {
        return new AuthResponse(accessToken, "Bearer", user);
    }
}
