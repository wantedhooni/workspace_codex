package com.portal.admin.api;

import com.portal.admin.auth.AuthInterceptor;
import com.portal.admin.config.AppSecurityProperties;
import static com.portal.admin.dto.AuthDtos.*;
import com.portal.admin.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Auth", description = "Authentication")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AppSecurityProperties securityProperties;

    public AuthController(AuthService authService, AppSecurityProperties securityProperties) {
        this.authService = authService;
        this.securityProperties = securityProperties;
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.LoginResult loginResult = authService.login(request.username(), request.password());
        return ResponseEntity.ok()
                .header("Set-Cookie", buildAuthCookie(loginResult.token(), -1).toString())
                .body(new LoginResponse(loginResult.token(), loginResult.user()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @CookieValue(name = AuthInterceptor.COOKIE_NAME, required = false) String token
    ) {
        authService.logout(token);
        return ResponseEntity.ok()
                .header("Set-Cookie", buildAuthCookie("", 0).toString())
                .body(Map.of("success", true));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@CookieValue(name = AuthInterceptor.COOKIE_NAME, required = false) String token) {
        try {
            return ResponseEntity.ok(authService.me(token));
        } catch (org.springframework.web.server.ResponseStatusException exception) {
            return ResponseEntity.status(exception.getStatusCode()).build();
        }
    }

    private ResponseCookie buildAuthCookie(String token, long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(AuthInterceptor.COOKIE_NAME, token)
                .httpOnly(true)
                .path("/")
                .sameSite(securityProperties.getCookie().getSameSite())
                .secure(securityProperties.getCookie().isSecure());
        if (maxAgeSeconds >= 0) {
            builder.maxAge(maxAgeSeconds);
        }
        return builder.build();
    }
}
