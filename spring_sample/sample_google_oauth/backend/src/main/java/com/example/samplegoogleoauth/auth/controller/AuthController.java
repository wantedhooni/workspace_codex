package com.example.samplegoogleoauth.auth.controller;

import com.example.samplegoogleoauth.auth.dto.AuthStatusResponse;
import com.example.samplegoogleoauth.auth.dto.SignupRequest;
import com.example.samplegoogleoauth.auth.dto.UserProfileResponse;
import com.example.samplegoogleoauth.auth.service.OAuthSignupService;
import jakarta.servlet.http.HttpServletRequest;
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

    public AuthController(OAuthSignupService oauthSignupService) {
        this.oauthSignupService = oauthSignupService;
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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) throws Exception {
        request.logout();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        return ResponseEntity.noContent().build();
    }
}
