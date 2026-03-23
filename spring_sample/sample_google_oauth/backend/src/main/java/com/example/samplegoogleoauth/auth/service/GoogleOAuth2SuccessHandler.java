package com.example.samplegoogleoauth.auth.service;

import com.example.samplegoogleoauth.auth.config.AppAuthProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Google OAuth 로그인 성공 시 프론트엔드로 복귀시키는 핸들러다.
 */
@Component
public class GoogleOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuthMemberSyncService oauthMemberSyncService;

    public GoogleOAuth2SuccessHandler(
        AppAuthProperties properties,
        OAuthMemberSyncService oauthMemberSyncService
    ) {
        this.oauthMemberSyncService = oauthMemberSyncService;
        setDefaultTargetUrl(properties.successRedirectUrl());
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        oauthMemberSyncService.syncOnLogin(authentication);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
