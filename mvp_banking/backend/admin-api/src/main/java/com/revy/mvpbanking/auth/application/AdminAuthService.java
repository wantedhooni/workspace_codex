package com.revy.mvpbanking.auth.application;

import com.revy.mvpbanking.admin.domain.AdminUserRepository;
import com.revy.mvpbanking.auth.domain.AuthenticatedPrincipal;
import com.revy.mvpbanking.auth.domain.AuthenticationResult;
import com.revy.mvpbanking.auth.domain.PrincipalType;
import com.revy.mvpbanking.auth.infrastructure.RefreshTokenStore;
import com.revy.mvpbanking.auth.presentation.LoginRequest;
import com.revy.mvpbanking.auth.presentation.RefreshTokenRequest;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@Transactional(readOnly = true)
public class AdminAuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;
    private final RefreshTokenStore refreshTokenStore;

    public AdminAuthService(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            TokenIssuer tokenIssuer,
            RefreshTokenStore refreshTokenStore
    ) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenIssuer = tokenIssuer;
        this.refreshTokenStore = refreshTokenStore;
    }

    public AuthenticationResult login(LoginRequest request) {
        var admin = adminUserRepository.findByEmail(request.email())
                .filter(candidate -> candidate.isActive() && passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid admin credentials"));

        var principal = new AuthenticatedPrincipal(
                admin.getId(),
                admin.getEmail(),
                admin.getDisplayName(),
                PrincipalType.ADMIN,
                List.of("ROLE_" + admin.getRole().name()),
                admin.isActive()
        );

        return new AuthenticationResult(principal, tokenIssuer.issue(principal));
    }

    public AuthenticationResult refresh(RefreshTokenRequest request) {
        var refreshRecord = refreshTokenStore.find(request.refreshToken())
                .filter(record -> record.principalType() == PrincipalType.ADMIN)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid refresh token"));

        tokenIssuer.revoke(request.refreshToken());

        var principal = new AuthenticatedPrincipal(
                refreshRecord.principalId(),
                refreshRecord.email(),
                refreshRecord.displayName(),
                refreshRecord.principalType(),
                refreshRecord.roles(),
                true
        );

        return new AuthenticationResult(principal, tokenIssuer.issue(principal));
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        tokenIssuer.revoke(request.refreshToken());
    }
}
