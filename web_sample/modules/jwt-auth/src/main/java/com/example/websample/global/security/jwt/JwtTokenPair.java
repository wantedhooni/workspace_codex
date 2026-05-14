package com.example.websample.global.security.jwt;

/** 액세스 토큰과 리프레시 토큰을 함께 반환하는 공통 응답 값입니다. */
public record JwtTokenPair(
        String accessToken,
        String refreshToken,
        String tokenType
) {
    public static JwtTokenPair bearer(String accessToken, String refreshToken) {
        return new JwtTokenPair(accessToken, refreshToken, "Bearer");
    }
}
