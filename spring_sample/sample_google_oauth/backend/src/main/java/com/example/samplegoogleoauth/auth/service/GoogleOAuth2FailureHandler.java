package com.example.samplegoogleoauth.auth.service;

import com.example.samplegoogleoauth.auth.config.AppAuthProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Google OAuth 로그인 실패 시 프론트엔드로 오류 상태를 전달하는 핸들러다.
 */
@Component
public class GoogleOAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final AppAuthProperties properties;

    public GoogleOAuth2FailureHandler(AppAuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException, ServletException {
        String redirectUrl = UriComponentsBuilder
            .fromUriString(properties.failureRedirectUrl())
            .queryParam("login", "error")
            .build(true)
            .toUriString();
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
