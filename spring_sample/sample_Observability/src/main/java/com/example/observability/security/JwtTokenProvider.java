package com.example.observability.security;

import com.example.observability.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * JWT 생성과 파싱을 담당한다.
 */
@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public TokenPayload generateToken(AuthenticatedUser authenticatedUser) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.expirationMinutes(), ChronoUnit.MINUTES);

        String token = Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(authenticatedUser.username())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim("tenantId", authenticatedUser.tenantId())
                .claim("displayName", authenticatedUser.displayName())
                .claim("role", authenticatedUser.role())
                .signWith(secretKey)
                .compact();

        return new TokenPayload(token, expiresAt, authenticatedUser);
    }

    public AuthenticatedUser parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new AuthenticatedUser(
                claims.get("tenantId", String.class),
                claims.getSubject(),
                claims.get("displayName", String.class),
                claims.get("role", String.class)
        );
    }

    /**
     * 발급된 토큰 본문과 사용자 정보를 함께 반환한다.
     */
    public record TokenPayload(
            String accessToken,
            Instant expiresAt,
            AuthenticatedUser authenticatedUser
    ) {
    }
}
