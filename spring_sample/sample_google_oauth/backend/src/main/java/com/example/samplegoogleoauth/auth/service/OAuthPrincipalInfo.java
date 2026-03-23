package com.example.samplegoogleoauth.auth.service;

/**
 * OAuth 인증 객체에서 추출한 핵심 사용자 정보를 표현한다.
 */
public record OAuthPrincipalInfo(
    String provider,
    String providerUserId,
    String name,
    String email,
    String picture
) {
}
