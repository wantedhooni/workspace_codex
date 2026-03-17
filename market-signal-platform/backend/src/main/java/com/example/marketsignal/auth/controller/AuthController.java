package com.example.marketsignal.auth;

import com.example.marketsignal.user.CurrentUser;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 공개 API를 제공한다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public AuthResponse signup(
            @Valid @RequestBody SignupRequest request,
            HttpServletResponse response
    ) {
        return authService.signup(request, response);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        return authService.login(request, response);
    }

    @PostMapping("/logout")
    public void logout(CurrentUser currentUser, HttpServletResponse response) {
        authService.logout(currentUser, response);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(
            @CookieValue(name = AuthService.REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        return authService.refresh(refreshToken, response);
    }
}
