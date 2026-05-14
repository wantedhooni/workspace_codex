package com.example.websample.global.security.jwt;

import com.example.websample.global.auth.LogoutRequest;
import com.example.websample.global.auth.RefreshTokenRequest;
import com.example.websample.global.error.BusinessException;
import com.example.websample.global.error.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.time.Duration;
import java.time.Instant;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/** Redis를 사용해 리프레시 토큰과 액세스 토큰 블랙리스트를 공통 관리하는 서비스입니다. */
@Service
public class JwtSessionService implements JwtTokenBlacklist {

    private static final String REFRESH_KEY_PREFIX = "jwt:refresh:";
    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtPrincipalResolver jwtPrincipalResolver;
    private final StringRedisTemplate redisTemplate;

    public JwtSessionService(
            JwtTokenProvider jwtTokenProvider,
            JwtPrincipalResolver jwtPrincipalResolver,
            StringRedisTemplate redisTemplate
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtPrincipalResolver = jwtPrincipalResolver;
        this.redisTemplate = redisTemplate;
    }

    /** 인증 주체에 대한 액세스 토큰과 리프레시 토큰을 발급하고 리프레시 토큰을 Redis에 저장합니다. */
    public JwtSession issue(JwtPrincipal principal) {
        String accessToken = jwtTokenProvider.createAccessToken(principal);
        String refreshToken = jwtTokenProvider.createRefreshToken(principal);
        Claims refreshClaims = jwtTokenProvider.parseClaims(refreshToken);
        saveRefreshToken(refreshToken, refreshClaims, principal);
        return new JwtSession(JwtTokenPair.bearer(accessToken, refreshToken), principal);
    }

    /** Redis에 저장된 리프레시 토큰을 검증하고 회전 발급합니다. */
    public JwtSession refresh(RefreshTokenRequest request, String expectedPrincipalType) {
        Claims claims = parseClaims(request.refreshToken());
        if (jwtTokenProvider.getTokenType(claims) != JwtTokenType.REFRESH) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        String principalType = claims.get(JwtTokenProvider.PRINCIPAL_TYPE_CLAIM, String.class);
        if (!expectedPrincipalType.equals(principalType)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String refreshKey = refreshKey(request.refreshToken());
        String storedValue = redisTemplate.opsForValue().get(refreshKey);
        if (storedValue == null) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        JwtPrincipal principal = jwtPrincipalResolver.resolve(principalType, Long.valueOf(claims.getSubject()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        String expectedValue = refreshValue(principal);
        if (!expectedValue.equals(storedValue)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        redisTemplate.delete(refreshKey);
        return issue(principal);
    }

    /** 로그아웃 요청의 액세스 토큰을 블랙리스트에 넣고 리프레시 토큰을 Redis에서 제거합니다. */
    public void logout(String authorization, LogoutRequest request) {
        String accessToken = extractBearerToken(authorization);
        Claims accessClaims = parseClaims(accessToken);
        if (jwtTokenProvider.getTokenType(accessClaims) != JwtTokenType.ACCESS) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        blacklist(accessClaims);

        if (request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
            redisTemplate.delete(refreshKey(request.refreshToken()));
        }
    }

    @Override
    public boolean contains(String tokenId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey(tokenId)));
    }

    private void saveRefreshToken(String refreshToken, Claims claims, JwtPrincipal principal) {
        if (!principal.principalType().equals(claims.get(JwtTokenProvider.PRINCIPAL_TYPE_CLAIM, String.class))
                || !principal.id().equals(Long.valueOf(claims.getSubject()))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        Duration ttl = ttlUntil(jwtTokenProvider.getExpiration(claims));
        redisTemplate.opsForValue().set(refreshKey(refreshToken), refreshValue(principal), ttl);
    }

    private void blacklist(Claims claims) {
        Duration ttl = ttlUntil(jwtTokenProvider.getExpiration(claims));
        redisTemplate.opsForValue().set(blacklistKey(jwtTokenProvider.getTokenId(claims)), "1", ttl);
    }

    private Claims parseClaims(String token) {
        try {
            return jwtTokenProvider.parseClaims(token);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    private Duration ttlUntil(Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }
        return ttl;
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return authorization.substring(BEARER_PREFIX.length());
    }

    private String refreshKey(String refreshToken) {
        return REFRESH_KEY_PREFIX + JwtTokenHash.sha256(refreshToken);
    }

    private String blacklistKey(String tokenId) {
        return BLACKLIST_KEY_PREFIX + tokenId;
    }

    private String refreshValue(JwtPrincipal principal) {
        return principal.principalType() + ":" + principal.id();
    }
}
