package com.revy.mvpbanking.auth.presentation;

import com.revy.mvpbanking.auth.application.AuthFacade;
import com.revy.mvpbanking.auth.application.UserAuthService;
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
@Profile("user-api")
@RequestMapping("/api/user")
public class UserAuthController {

    private final UserAuthService userAuthService;
    private final AuthFacade authFacade;
    private final AuditLogService auditLogService;

    public UserAuthController(
            UserAuthService userAuthService,
            AuthFacade authFacade,
            AuditLogService auditLogService
    ) {
        this.userAuthService = userAuthService;
        this.authFacade = authFacade;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/auth/signup")
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        var result = userAuthService.signup(request);
        auditLogService.logSystem(AuditActionType.USER_SIGNUP, "END_USER", request.email(), "User signup completed");
        return ApiResponse.ok(AuthResponse.of(result));
    }

    @PostMapping("/auth/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = userAuthService.login(request);
        auditLogService.logSystem(AuditActionType.USER_LOGIN, "END_USER", request.email(), "User login completed");
        return ApiResponse.ok(AuthResponse.of(result));
    }

    @PostMapping("/auth/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var result = userAuthService.refresh(request);
        auditLogService.logSystem(AuditActionType.USER_REFRESH, "END_USER", result.principal().email(), "User token refreshed");
        return ApiResponse.ok(AuthResponse.of(result));
    }

    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        userAuthService.logout(request);
        auditLogService.logSystem(AuditActionType.USER_LOGOUT, "END_USER", "logout", "User logout completed");
    }

    @GetMapping("/me")
    public ApiResponse<PrincipalProfileResponse> me() {
        return ApiResponse.ok(authFacade.currentPrincipal());
    }
}
