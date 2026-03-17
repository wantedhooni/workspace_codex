package com.example.multitenancy.service;

import com.example.multitenancy.domain.AppUser;
import com.example.multitenancy.repository.AppUserRepository;
import com.example.multitenancy.security.AuthenticatedUser;
import com.example.multitenancy.security.JwtTokenProvider;
import com.example.multitenancy.web.dto.LoginRequest;
import com.example.multitenancy.web.dto.LoginResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 테넌트별 로그인과 JWT 발급을 처리한다.
 */
@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 입력된 테넌트와 계정을 검증한 뒤 JWT를 발급한다.
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
                        appUser.getRole()
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
