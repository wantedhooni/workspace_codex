package com.example.samplegoogleoauth.auth.service;

import com.example.samplegoogleoauth.auth.dto.TokenResponse;
import com.example.samplegoogleoauth.auth.entity.OAuthMember;
import com.example.samplegoogleoauth.auth.repository.OAuthMemberRepository;
import com.example.samplegoogleoauth.auth.security.JwtTokenProvider;
import com.example.samplegoogleoauth.auth.security.RefreshTokenService;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

/**
 * JWT 발급, 재발급, 로그아웃 시 refresh token 상태를 관리한다.
 */
@Service
public class AuthTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final OAuthMemberRepository oauthMemberRepository;

    public AuthTokenService(
        JwtTokenProvider jwtTokenProvider,
        RefreshTokenService refreshTokenService,
        OAuthMemberRepository oauthMemberRepository
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.oauthMemberRepository = oauthMemberRepository;
    }

    /**
     * OAuth 로그인 성공 후 회원 기준으로 JWT 쌍을 발급하고 Redis에 refresh token을 저장한다.
     */
    public TokenResponse issueTokens(OAuthMember member) {
        TokenResponse tokenResponse = jwtTokenProvider.issueTokenPair(member);
        refreshTokenService.save(member.getId(), tokenResponse.refreshToken(), jwtTokenProvider.refreshTokenTtl());
        return tokenResponse;
    }

    /**
     * refresh token을 검증하고 새로운 JWT 쌍을 발급한다.
     */
    public TokenResponse refresh(String refreshToken) {
        Claims claims = jwtTokenProvider.parseRefreshToken(refreshToken);
        Long memberId = jwtTokenProvider.extractMemberId(claims);

        if (!refreshTokenService.matches(memberId, refreshToken)) {
            throw new IllegalStateException("Redis에 저장된 refresh token과 일치하지 않습니다.");
        }

        OAuthMember member = oauthMemberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));

        refreshTokenService.revoke(memberId);
        return issueTokens(member);
    }

    /**
     * 로그아웃 시 refresh token을 Redis에서 제거한다.
     */
    public void logout(Long memberId, String refreshToken) {
        Claims claims = jwtTokenProvider.parseRefreshToken(refreshToken);
        Long tokenMemberId = jwtTokenProvider.extractMemberId(claims);

        if (!memberId.equals(tokenMemberId) || !refreshTokenService.matches(memberId, refreshToken)) {
            throw new IllegalStateException("로그아웃 토큰 검증에 실패했습니다.");
        }

        refreshTokenService.revoke(memberId);
    }
}
