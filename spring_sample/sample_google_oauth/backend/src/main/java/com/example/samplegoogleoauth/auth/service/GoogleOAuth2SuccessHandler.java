package com.example.samplegoogleoauth.auth.service;

import com.example.samplegoogleoauth.auth.config.AppAuthProperties;
import com.example.samplegoogleoauth.auth.dto.TokenResponse;
import com.example.samplegoogleoauth.auth.entity.OAuthMember;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Google OAuth 로그인 성공 시 회원을 동기화하고 JWT를 발급한 뒤 프론트엔드로 복귀시킨다.
 */
@Component
public class GoogleOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AppAuthProperties properties;
    private final OAuthMemberSyncService oauthMemberSyncService;
    private final AuthTokenService authTokenService;

    public GoogleOAuth2SuccessHandler(
        AppAuthProperties properties,
        OAuthMemberSyncService oauthMemberSyncService,
        AuthTokenService authTokenService
    ) {
        this.properties = properties;
        this.oauthMemberSyncService = oauthMemberSyncService;
        this.authTokenService = authTokenService;
        setDefaultTargetUrl(properties.successRedirectUrl());
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        OAuthMember member = oauthMemberSyncService.syncOnLogin(authentication);
        TokenResponse tokenResponse = authTokenService.issueTokens(member);

        String redirectUrl = UriComponentsBuilder
            .fromUriString(properties.successRedirectUrl())
            .queryParam("login", "success")
            .queryParam("accessToken", tokenResponse.accessToken())
            .queryParam("refreshToken", tokenResponse.refreshToken())
            .build(true)
            .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
