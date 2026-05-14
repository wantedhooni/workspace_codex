package com.example.websample.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/** JWT 액세스 토큰의 생성과 파싱을 담당하는 모듈 서비스입니다. */
@Component
public class JwtTokenProvider {

    public static final String PRINCIPAL_TYPE_CLAIM = "principalType";
    public static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String EMAIL_CLAIM = "email";
    private static final String ROLE_CLAIM = "role";

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(JwtPrincipal principal) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.accessTokenExpirationMinutes() * 60);
        return createToken(principal, JwtTokenType.ACCESS, now, expiresAt);
    }

    public String createRefreshToken(JwtPrincipal principal) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.refreshTokenExpirationDays() * 24 * 60 * 60);
        return createToken(principal, JwtTokenType.REFRESH, now, expiresAt);
    }

    private String createToken(JwtPrincipal principal, JwtTokenType tokenType, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .issuer(properties.issuer())
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(principal.id()))
                .claim(PRINCIPAL_TYPE_CLAIM, principal.principalType())
                .claim(TOKEN_TYPE_CLAIM, tokenType.name())
                .claim(EMAIL_CLAIM, principal.email())
                .claim(ROLE_CLAIM, principal.role())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public JwtTokenType getTokenType(Claims claims) {
        return JwtTokenType.valueOf(claims.get(TOKEN_TYPE_CLAIM, String.class));
    }

    public String getTokenId(Claims claims) {
        return claims.getId();
    }

    public Instant getExpiration(Claims claims) {
        return claims.getExpiration().toInstant();
    }
}
