package com.example.marketsignal.auth;

import com.example.marketsignal.common.BusinessException;
import com.example.marketsignal.config.AppProperties;
import com.example.marketsignal.user.CurrentUser;
import com.example.marketsignal.user.CustomUserDetails;
import com.example.marketsignal.user.User;
import com.example.marketsignal.user.UserProfileResponse;
import com.example.marketsignal.user.UserRepository;
import com.example.marketsignal.user.UserRole;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원가입, 로그인, 로그아웃, 토큰 재발급을 처리한다.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AppProperties appProperties;

    /**
     * 신규 사용자를 가입시키고 즉시 access token을 발급한다.
     */
    @Transactional
    public AuthResponse signup(SignupRequest request, HttpServletResponse response) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다.");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .bio("시장 신호를 추적하는 투자자")
                .role(UserRole.ROLE_USER)
                .build();
        userRepository.save(user);
        return issueTokens(user, response);
    }

    /**
     * 자격 증명을 검증하고 토큰을 발급한다.
     */
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        return issueTokens(user, response);
    }

    /**
     * refresh token을 사용해 새 access token을 발급한다.
     */
    @Transactional
    public AuthResponse refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || !jwtTokenProvider.isValid(refreshToken)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "유효한 refresh token이 없습니다.");
        }

        String email = jwtTokenProvider.extractSubject(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "refresh token이 일치하지 않습니다.");
        }

        if (user.getRefreshTokenExpiresAt() == null || user.getRefreshTokenExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "refresh token이 만료되었습니다.");
        }

        return issueTokens(user, response);
    }

    /**
     * 현재 사용자 세션을 종료하고 refresh token을 제거한다.
     */
    @Transactional
    public void logout(CurrentUser currentUser, HttpServletResponse response) {
        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        user.clearRefreshToken();
        clearRefreshTokenCookie(response);
    }

    /**
     * 사용자 기준 access token과 refresh token을 동시 발급한다.
     */
    @Transactional
    public AuthResponse issueTokens(User user, HttpServletResponse response) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(user.getId(), user.getEmail(), user.getPassword(), user.getRole()),
                null,
                List.of(user.getRole().name()).stream()
                        .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                        .toList()
        );
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        user.updateRefreshToken(refreshToken, jwtTokenProvider.extractExpiration(refreshToken));
        writeRefreshTokenCookie(refreshToken, response);
        return AuthResponse.of(accessToken, UserProfileResponse.from(user));
    }

    private void writeRefreshTokenCookie(String refreshToken, HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge((int) (appProperties.jwt().refreshTokenExpirationDays() * 24 * 60 * 60));
        response.addCookie(cookie);
        response.addHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.SET_COOKIE);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
