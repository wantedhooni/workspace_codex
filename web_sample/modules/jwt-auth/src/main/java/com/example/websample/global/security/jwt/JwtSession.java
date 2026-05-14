package com.example.websample.global.security.jwt;

/** JWT 세션 발급 결과와 인증 주체를 함께 담는 공통 값입니다. */
public record JwtSession(
        JwtTokenPair tokens,
        JwtPrincipal principal
) {
}
