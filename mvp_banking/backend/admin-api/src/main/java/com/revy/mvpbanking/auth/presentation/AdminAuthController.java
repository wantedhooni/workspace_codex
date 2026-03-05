package com.revy.mvpbanking.auth.presentation;

import com.revy.mvpbanking.auth.application.AdminAuthService;
import com.revy.mvpbanking.auth.application.AuthFacade;
import com.revy.mvpbanking.audit.application.AuditLogService;
import com.revy.mvpbanking.audit.domain.AuditActionType;
import com.revy.mvpbanking.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("admin-api")
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final AuthFacade authFacade;
    private final AuditLogService auditLogService;

    public AdminAuthController(
            AdminAuthService adminAuthService,
            AuthFacade authFacade,
            AuditLogService auditLogService
    ) {
        this.adminAuthService = adminAuthService;
        this.authFacade = authFacade;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/auth/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = adminAuthService.login(request);
        auditLogService.logSystem(AuditActionType.ADMIN_LOGIN, "ADMIN_USER", request.email(), "Admin login completed");
        return ApiResponse.ok(AuthResponse.of(result));
    }

    @PostMapping("/auth/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var result = adminAuthService.refresh(request);
        auditLogService.logSystem(AuditActionType.ADMIN_REFRESH, "ADMIN_USER", result.principal().email(), "Admin token refreshed");
        return ApiResponse.ok(AuthResponse.of(result));
    }

    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        adminAuthService.logout(request);
        auditLogService.logSystem(AuditActionType.ADMIN_LOGOUT, "ADMIN_USER", "logout", "Admin logout completed");
    }

    @GetMapping("/me")
    public ApiResponse<PrincipalProfileResponse> me() {
        return ApiResponse.ok(authFacade.currentPrincipal());
    }
}
