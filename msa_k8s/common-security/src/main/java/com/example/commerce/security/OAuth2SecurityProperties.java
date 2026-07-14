package com.example.commerce.security;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 서비스 공통 OAuth2 및 Keycloak 보안 설정을 바인딩한다.
 *
 * @param enabled Resource Server JWT 검증 활성화 여부
 * @param clientEnabled OAuth2 Client 로그인 활성화 여부
 * @param clientId Keycloak client role을 추출할 대상 클라이언트 식별자
 * @param allowedAudiences JWT audience 검증에 사용할 허용 audience 목록
 */
@ConfigurationProperties("commerce.security.oauth2")
public record OAuth2SecurityProperties(
        boolean enabled,
        boolean clientEnabled,
        String clientId,
        List<String> allowedAudiences) {

    /**
     * 누락된 선택 설정에 운영 기본값을 적용한다.
     */
    public OAuth2SecurityProperties {
        if (clientId == null || clientId.isBlank()) {
            clientId = "commerce-api";
        }
        if (allowedAudiences == null) {
            allowedAudiences = List.of();
        } else {
            allowedAudiences = List.copyOf(allowedAudiences);
        }
    }
}
