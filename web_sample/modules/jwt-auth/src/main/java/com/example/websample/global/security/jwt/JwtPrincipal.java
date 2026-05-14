package com.example.websample.global.security.jwt;

/** JWT 인증 모듈이 도메인 엔티티와 무관하게 다루는 인증 주체 계약입니다. */
public interface JwtPrincipal {

    Long id();

    String email();

    String role();

    String principalType();
}
