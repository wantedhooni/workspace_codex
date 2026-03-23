package com.example.samplegoogleoauth.auth.service;

import java.util.Map;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * Spring Security 인증 객체를 서비스 계층에서 사용할 OAuth 사용자 정보로 변환한다.
 */
@Component
public class OAuthPrincipalResolver {

    /**
     * 현재 인증된 OAuth 사용자의 공급자, 식별자, 기본 프로필 정보를 추출한다.
     */
    public OAuthPrincipalInfo resolve(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("인증된 사용자만 처리할 수 있습니다.");
        }

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            throw new IllegalStateException("OAuth2 인증 정보가 아닙니다.");
        }

        Object principal = oauthToken.getPrincipal();
        if (!(principal instanceof OAuth2User oauth2User)) {
            throw new IllegalStateException("OAuth2 사용자 정보를 찾을 수 없습니다.");
        }

        Map<String, Object> attributes = oauth2User.getAttributes();
        String provider = oauthToken.getAuthorizedClientRegistrationId().toUpperCase();

        return new OAuthPrincipalInfo(
            provider,
            stringValue(attributes.get("sub")),
            stringValue(attributes.get("name")),
            stringValue(attributes.get("email")),
            stringValue(attributes.get("picture"))
        );
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
