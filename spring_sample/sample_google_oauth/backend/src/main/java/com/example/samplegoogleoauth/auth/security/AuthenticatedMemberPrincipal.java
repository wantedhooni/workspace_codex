package com.example.samplegoogleoauth.auth.security;

/**
 * JWT 인증 후 SecurityContext에 저장할 회원 식별 정보다.
 */
public record AuthenticatedMemberPrincipal(
    Long memberId,
    String provider,
    String providerUserId,
    String email
) {
}
