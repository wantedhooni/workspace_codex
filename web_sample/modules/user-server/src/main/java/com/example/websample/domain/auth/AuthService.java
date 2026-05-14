package com.example.websample.domain.auth;

import com.example.websample.domain.user.User;
import com.example.websample.domain.user.UserJwtPrincipal;
import com.example.websample.domain.user.UserRepository;
import com.example.websample.global.auth.LoginRequest;
import com.example.websample.global.auth.LogoutRequest;
import com.example.websample.global.auth.RefreshTokenRequest;
import com.example.websample.global.error.BusinessException;
import com.example.websample.global.error.ErrorCode;
import com.example.websample.global.security.jwt.JwtSessionService;
import com.example.websample.global.security.jwt.JwtSession;
import com.example.websample.global.security.jwt.JwtTokenPair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 인증 관련 비즈니스 로직을 처리하는 서비스입니다. */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtSessionService jwtSessionService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtSessionService jwtSessionService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtSessionService = jwtSessionService;
    }

    /** 새 사용자를 등록하고 즉시 사용할 수 있는 액세스 토큰을 발급합니다. */
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name()
        );
        User savedUser = userRepository.save(user);
        return issueTokens(savedUser);
    }

    /** 이메일과 비밀번호를 검증한 뒤 액세스 토큰을 발급합니다. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(user);
    }

    /** 저장된 리프레시 토큰을 검증하고 토큰을 회전 발급합니다. */
    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        JwtSession session = jwtSessionService.refresh(request, UserJwtPrincipal.TYPE);
        JwtTokenPair tokenPair = session.tokens();
        User user = userRepository.findById(session.principal().id())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return AuthResponse.of(tokenPair.accessToken(), tokenPair.refreshToken(), user);
    }

    /** 로그아웃 요청의 액세스 토큰을 블랙리스트에 넣고 리프레시 토큰을 폐기합니다. */
    @Transactional
    public void logout(String authorization, LogoutRequest request) {
        jwtSessionService.logout(authorization, request);
    }

    private AuthResponse issueTokens(User user) {
        JwtTokenPair tokenPair = jwtSessionService.issue(UserJwtPrincipal.from(user)).tokens();
        return AuthResponse.of(tokenPair.accessToken(), tokenPair.refreshToken(), user);
    }
}
