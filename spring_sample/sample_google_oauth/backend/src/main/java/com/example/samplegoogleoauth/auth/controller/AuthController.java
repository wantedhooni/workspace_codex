package com.example.samplegoogleoauth.auth.controller;

import com.example.samplegoogleoauth.auth.dto.AuthStatusResponse;
import com.example.samplegoogleoauth.auth.dto.LogoutRequest;
import com.example.samplegoogleoauth.auth.dto.RefreshTokenRequest;
import com.example.samplegoogleoauth.auth.dto.SignupRequest;
import com.example.samplegoogleoauth.auth.dto.TokenResponse;
import com.example.samplegoogleoauth.auth.dto.UserProfileResponse;
import com.example.samplegoogleoauth.auth.security.AuthenticatedMemberPrincipal;
import com.example.samplegoogleoauth.auth.service.AuthTokenService;
import com.example.samplegoogleoauth.auth.service.OAuthSignupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인 상태 조회와 로그아웃에 필요한 인증 API를 제공한다.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final OAuthSignupService oauthSignupService;
    private final AuthTokenService authTokenService;

    public AuthController(
        OAuthSignupService oauthSignupService,
        AuthTokenService authTokenService
    ) {
        this.oauthSignupService = oauthSignupService;
        this.authTokenService = authTokenService;
    }

    @GetMapping("/status")
    public AuthStatusResponse status(Authentication authentication) {
        boolean authenticated = authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
        return new AuthStatusResponse(authenticated);
    }

    @GetMapping("/me")
    public UserProfileResponse currentUser(Authentication authentication) {
        return oauthSignupService.getCurrentUserProfile(authentication);
    }

    @PostMapping("/signup")
    public UserProfileResponse signup(
        Authentication authentication,
        @Valid @org.springframework.web.bind.annotation.RequestBody SignupRequest request
    ) {
        return oauthSignupService.signup(authentication, request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @org.springframework.web.bind.annotation.RequestBody RefreshTokenRequest request) {
        return authTokenService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        Authentication authentication,
        @Valid @org.springframework.web.bind.annotation.RequestBody LogoutRequest request
    ) {
        if (!(authentication.getPrincipal() instanceof AuthenticatedMemberPrincipal principal)) {
            throw new IllegalStateException("JWT 인증 정보가 아닙니다.");
        }

        authTokenService.logout(principal.memberId(), request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
