package com.example.samplegoogleoauth.auth.security;

import com.example.samplegoogleoauth.auth.config.JwtProperties;
import com.example.samplegoogleoauth.auth.dto.TokenResponse;
import com.example.samplegoogleoauth.auth.entity.OAuthMember;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * JWT access token과 refresh token을 발급하고 검증한다.
 */
@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String MEMBER_ID_CLAIM = "memberId";
    private static final String PROVIDER_CLAIM = "provider";
    private static final String PROVIDER_USER_ID_CLAIM = "providerUserId";
    private static final String EMAIL_CLAIM = "email";
    private static final String JTI_CLAIM = "jti";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * API 호출용 access token을 발급한다.
     */
    public TokenResponse issueTokenPair(OAuthMember member) {
        Instant now = Instant.now();
        Instant accessExpiresAt = now.plus(Duration.ofMinutes(jwtProperties.accessTokenMinutes()));
        Instant refreshExpiresAt = now.plus(Duration.ofDays(jwtProperties.refreshTokenDays()));

        String accessToken = Jwts.builder()
            .subject(String.valueOf(member.getId()))
            .claim(TOKEN_TYPE_CLAIM, "ACCESS")
            .claim(MEMBER_ID_CLAIM, member.getId())
            .claim(PROVIDER_CLAIM, member.getProvider())
            .claim(PROVIDER_USER_ID_CLAIM, member.getProviderUserId())
            .claim(EMAIL_CLAIM, member.getEmail())
            .issuedAt(Date.from(now))
            .expiration(Date.from(accessExpiresAt))
            .signWith(secretKey)
            .compact();

        String refreshToken = Jwts.builder()
            .subject(String.valueOf(member.getId()))
            .claim(TOKEN_TYPE_CLAIM, "REFRESH")
            .claim(MEMBER_ID_CLAIM, member.getId())
            .claim(JTI_CLAIM, UUID.randomUUID().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(refreshExpiresAt))
            .signWith(secretKey)
            .compact();

        return new TokenResponse(
            accessToken,
            refreshToken,
            "Bearer",
            Duration.between(now, accessExpiresAt).toSeconds()
        );
    }

    /**
     * access token을 검증하고 클레임을 반환한다.
     */
    public Claims parseAccessToken(String token) {
        return parseToken(token, "ACCESS").getPayload();
    }

    /**
     * refresh token을 검증하고 클레임을 반환한다.
     */
    public Claims parseRefreshToken(String token) {
        return parseToken(token, "REFRESH").getPayload();
    }

    public long extractMemberId(Claims claims) {
        Object memberId = claims.get(MEMBER_ID_CLAIM);
        if (memberId instanceof Integer integerValue) {
            return integerValue.longValue();
        }
        if (memberId instanceof Long longValue) {
            return longValue;
        }
        return Long.parseLong(String.valueOf(memberId));
    }

    public Duration refreshTokenTtl() {
        return Duration.ofDays(jwtProperties.refreshTokenDays());
    }

    public String extractRefreshTokenId(Claims claims) {
        return claims.get(JTI_CLAIM, String.class);
    }

    private Jws<Claims> parseToken(String token, String expectedType) {
        try {
            Jws<Claims> jws = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);

            String tokenType = jws.getPayload().get(TOKEN_TYPE_CLAIM, String.class);
            if (!expectedType.equals(tokenType)) {
                throw new IllegalStateException("토큰 유형이 올바르지 않습니다.");
            }

            return jws;
        } catch (JwtException | IllegalArgumentException exception) {
            throw new IllegalStateException("유효하지 않은 토큰입니다.");
        }
    }
}
