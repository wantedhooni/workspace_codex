package com.example.commerce.security;

import reactor.core.publisher.Mono;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;

/**
 * Servlet과 Reactive 서비스에서 동일한 Keycloak JWT 권한 변환 정책을 사용하도록 변환기를 제공한다.
 */
public final class KeycloakJwtAuthenticationConverters {

    private KeycloakJwtAuthenticationConverters() {
    }

    /**
     * Servlet 기반 Resource Server용 JWT 인증 변환기를 생성한다.
     *
     * @param clientId Keycloak client role을 읽을 클라이언트 식별자
     * @return Servlet JWT 인증 변환기
     */
    public static Converter<Jwt, AbstractAuthenticationToken> servlet(String clientId) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakJwtGrantedAuthoritiesConverter(clientId));
        return converter;
    }

    /**
     * WebFlux 기반 Resource Server용 JWT 인증 변환기를 생성한다.
     *
     * @param clientId Keycloak client role을 읽을 클라이언트 식별자
     * @return Reactive JWT 인증 변환기
     */
    public static Converter<Jwt, Mono<AbstractAuthenticationToken>> reactive(String clientId) {
        return new ReactiveJwtAuthenticationConverterAdapter(servlet(clientId));
    }
}
