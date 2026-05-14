package com.example.websample.domain.admin;

import com.example.websample.global.auth.LoginRequest;
import com.example.websample.global.auth.LogoutRequest;
import com.example.websample.global.auth.RefreshTokenRequest;
import com.example.websample.global.common.ApiResponse;
import com.example.websample.global.security.jwt.JwtPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 관리자 로그인과 관리자 인증 확인 API를 제공하는 컨트롤러입니다. */
@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/auth/login")
    public ApiResponse<AdminAuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(adminAuthService.login(request));
    }

    @PostMapping("/auth/refresh")
    public ApiResponse<AdminAuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(adminAuthService.refresh(request));
    }

    @PostMapping("/auth/logout")
    public ApiResponse<Void> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody(required = false) LogoutRequest request
    ) {
        adminAuthService.logout(authorization, request);
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<JwtPrincipal> me(@AuthenticationPrincipal JwtPrincipal principal) {
        return ApiResponse.ok(principal);
    }
}
