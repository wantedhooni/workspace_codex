package com.example.observability.service;

import com.example.observability.domain.AppUser;
import com.example.observability.repository.AppUserRepository;
import com.example.observability.security.AuthenticatedUser;
import com.example.observability.security.JwtTokenProvider;
import com.example.observability.web.dto.LoginRequest;
import com.example.observability.web.dto.LoginResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 테넌트 기반 로그인 검증과 JWT 발급을 담당한다.
 */
@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 입력된 테넌트와 계정 정보를 검증한 뒤 액세스 토큰을 발급한다.
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        AppUser appUser = appUserRepository.findByTenantIdAndUsername(request.tenantId(), request.username())
                .orElseThrow(() -> new IllegalArgumentException("테넌트 또는 계정 정보가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), appUser.getPasswordHash())) {
            throw new IllegalArgumentException("테넌트 또는 계정 정보가 올바르지 않습니다.");
        }

        JwtTokenProvider.TokenPayload tokenPayload = jwtTokenProvider.generateToken(
                new AuthenticatedUser(
                        appUser.getTenantId(),
                        appUser.getUsername(),
                        appUser.getDisplayName(),
                        appUser.getRole().name()
                )
        );

        return new LoginResponse(
                tokenPayload.accessToken(),
                tokenPayload.expiresAt(),
                new LoginResponse.UserProfile(
                        tokenPayload.authenticatedUser().tenantId(),
                        tokenPayload.authenticatedUser().username(),
                        tokenPayload.authenticatedUser().displayName(),
                        tokenPayload.authenticatedUser().role()
                )
        );
    }
}
