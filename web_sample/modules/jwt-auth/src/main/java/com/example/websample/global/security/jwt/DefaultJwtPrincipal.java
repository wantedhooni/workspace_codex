package com.example.websample.global.security.jwt;

/** JWT 인증 이후 SecurityContext에 저장되는 기본 인증 주체입니다. */
public record DefaultJwtPrincipal(
        Long id,
        String email,
        String role,
        String principalType
) implements JwtPrincipal {
}
