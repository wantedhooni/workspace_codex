package com.quant.mvp.api;

import com.quant.mvp.config.JwtTokenService;
import com.quant.mvp.pipeline.payload.LoginPayload;
import com.quant.mvp.pipeline.payload.PasswordResetConfirmPayload;
import com.quant.mvp.pipeline.service.AccessControlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccessControlService accessControlService;
    private final JwtTokenService jwtTokenService;

    public AuthController(
            AccessControlService accessControlService,
            JwtTokenService jwtTokenService
    ) {
        this.accessControlService = accessControlService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public LoginPayload.Res login(@Valid @RequestBody LoginPayload.Req req, HttpServletRequest httpRequest) {
        AccessControlService.AuthenticatedUser authenticated = accessControlService.authenticate(
                req.email(),
                req.password(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        );

        JwtTokenService.TokenIssue tokenIssue = jwtTokenService.issueToken(
                authenticated.userId(),
                authenticated.email(),
                authenticated.name(),
                authenticated.roleCodes()
        );

        return new LoginPayload.Res(
                tokenIssue.accessToken(),
                "Bearer",
                tokenIssue.expiresInSeconds(),
                authenticated.userId(),
                authenticated.email(),
                authenticated.name(),
                authenticated.roleCodes()
        );
    }

    @PostMapping("/password-resets/confirm")
    public PasswordResetConfirmPayload.Res confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmPayload.Req req
    ) {
        AccessControlService.PasswordResetConfirmResult result = accessControlService.confirmPasswordReset(
                req.token(),
                req.newPassword()
        );
        return new PasswordResetConfirmPayload.Res(
                result.userId(),
                result.changed(),
                result.changedAt()
        );
    }
}
