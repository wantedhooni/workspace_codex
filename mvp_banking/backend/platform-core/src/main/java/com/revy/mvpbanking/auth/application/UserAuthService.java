package com.revy.mvpbanking.auth.application;

import com.revy.mvpbanking.auth.domain.AuthenticatedPrincipal;
import com.revy.mvpbanking.auth.domain.AuthenticationResult;
import com.revy.mvpbanking.auth.domain.PrincipalType;
import com.revy.mvpbanking.auth.infrastructure.RefreshTokenStore;
import com.revy.mvpbanking.auth.presentation.LoginRequest;
import com.revy.mvpbanking.auth.presentation.RefreshTokenRequest;
import com.revy.mvpbanking.auth.presentation.SignupRequest;
import com.revy.mvpbanking.user.domain.EndUser;
import com.revy.mvpbanking.user.domain.EndUserRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@Transactional(readOnly = true)
public class UserAuthService {

    private final EndUserRepository endUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;
    private final RefreshTokenStore refreshTokenStore;

    public UserAuthService(
            EndUserRepository endUserRepository,
            PasswordEncoder passwordEncoder,
            TokenIssuer tokenIssuer,
            RefreshTokenStore refreshTokenStore
    ) {
        this.endUserRepository = endUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenIssuer = tokenIssuer;
        this.refreshTokenStore = refreshTokenStore;
    }

    @Transactional
    public AuthenticationResult signup(SignupRequest request) {
        if (endUserRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(CONFLICT, "Email already exists");
        }

        EndUser endUser = endUserRepository.save(
                new EndUser(
                        request.email(),
                        passwordEncoder.encode(request.password()),
                        request.fullName(),
                        true
                )
        );

        var principal = new AuthenticatedPrincipal(
                endUser.getId(),
                endUser.getEmail(),
                endUser.getFullName(),
                PrincipalType.USER,
                List.of("ROLE_USER"),
                endUser.isActive()
        );

        return new AuthenticationResult(principal, tokenIssuer.issue(principal));
    }

    public AuthenticationResult login(LoginRequest request) {
        var user = endUserRepository.findByEmail(request.email())
                .filter(candidate -> candidate.isActive() && passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid user credentials"));

        var principal = new AuthenticatedPrincipal(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                PrincipalType.USER,
                List.of("ROLE_USER"),
                user.isActive()
        );

        return new AuthenticationResult(principal, tokenIssuer.issue(principal));
    }

    public AuthenticationResult refresh(RefreshTokenRequest request) {
        var refreshRecord = refreshTokenStore.find(request.refreshToken())
                .filter(record -> record.principalType() == PrincipalType.USER)
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
