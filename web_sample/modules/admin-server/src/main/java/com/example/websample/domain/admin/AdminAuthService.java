package com.example.websample.domain.admin;

import com.example.websample.global.auth.LoginRequest;
import com.example.websample.global.auth.LogoutRequest;
import com.example.websample.global.auth.RefreshTokenRequest;
import com.example.websample.global.error.BusinessException;
import com.example.websample.global.error.ErrorCode;
import com.example.websample.global.security.jwt.JwtSession;
import com.example.websample.global.security.jwt.JwtSessionService;
import com.example.websample.global.security.jwt.JwtTokenPair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 관리자 인증 관련 비즈니스 로직을 처리하는 서비스입니다. */
@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtSessionService jwtSessionService;

    public AdminAuthService(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            JwtSessionService jwtSessionService
    ) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtSessionService = jwtSessionService;
    }

    /** 관리자 이메일과 비밀번호를 검증한 뒤 ADMIN 주체 액세스 토큰을 발급합니다. */
    @Transactional(readOnly = true)
    public AdminAuthResponse login(LoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(admin);
    }

    /** 저장된 관리자 리프레시 토큰을 검증하고 토큰을 회전 발급합니다. */
    @Transactional(readOnly = true)
    public AdminAuthResponse refresh(RefreshTokenRequest request) {
        JwtSession session = jwtSessionService.refresh(request, AdminJwtPrincipal.TYPE);
        JwtTokenPair tokenPair = session.tokens();
        Admin admin = adminRepository.findById(session.principal().id())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return AdminAuthResponse.of(tokenPair.accessToken(), tokenPair.refreshToken(), admin);
    }

    /** 로그아웃 요청의 관리자 액세스 토큰을 블랙리스트에 넣고 리프레시 토큰을 폐기합니다. */
    @Transactional
    public void logout(String authorization, LogoutRequest request) {
        jwtSessionService.logout(authorization, request);
    }

    private AdminAuthResponse issueTokens(Admin admin) {
        JwtTokenPair tokenPair = jwtSessionService.issue(AdminJwtPrincipal.from(admin)).tokens();
        return AdminAuthResponse.of(tokenPair.accessToken(), tokenPair.refreshToken(), admin);
    }
}
