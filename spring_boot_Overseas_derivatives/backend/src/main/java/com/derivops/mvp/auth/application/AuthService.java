package com.derivops.mvp.auth.application;
import com.derivops.mvp.auth.api.*;
import com.derivops.mvp.auth.dto.*;


import com.derivops.mvp.audit.application.AuditLogService;
import com.derivops.mvp.common.BadRequestException;
import com.derivops.mvp.common.UnauthorizedException;
import com.derivops.mvp.user.UserAccount;
import com.derivops.mvp.user.infrastructure.UserAccountRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private static final org.slf4j.Logger SECURITY_AUDIT = org.slf4j.LoggerFactory.getLogger("SECURITY_AUDIT");

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            UserAccount user = userAccountRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new BadRequestException("User not found"));

            LoginResponse response = issueTokens(user);
            auditLogService.log(user.getUsername(), "LOGIN_SUCCESS", "AUTH", user.getUsername(), "Successful login");

            return response;
        } catch (BadCredentialsException ex) {
            SECURITY_AUDIT.warn("login failed username={} reason=bad_credentials", request.username());
            throw new BadRequestException("Invalid username or password");
        }
    }

    public LoginResponse refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.parseClaims(refreshToken);
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        if (!jwtService.isRefreshToken(claims)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        UserAccount user = userAccountRepository.findByUsername(claims.getSubject())
                .filter(UserAccount::isActive)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        return issueTokens(user);
    }

    private LoginResponse issueTokens(UserAccount user) {
        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());
        return new LoginResponse(
                accessToken,
                refreshToken,
                jwtService.getAccessExpirationSeconds(),
                jwtService.getRefreshExpirationSeconds(),
                user.getRole().name()
        );
    }
}
