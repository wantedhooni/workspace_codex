package com.example.marketsignal.auth;

import com.example.marketsignal.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성과 검증을 담당한다.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final AppProperties.Jwt jwtProperties;

    public JwtTokenProvider(AppProperties appProperties) {
        this.jwtProperties = appProperties.jwt();
        byte[] rawKey = jwtProperties.secret().length() >= 32
                ? jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
                : Decoders.BASE64.decode(jwtProperties.secret());
        this.secretKey = Keys.hmacShaKeyFor(rawKey);
    }

    /**
     * 인증 정보 기반 access token을 생성한다.
     */
    public String generateAccessToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.accessTokenExpirationMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(authentication.getName())
                .claim("scope", authentication.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * refresh token 문자열을 생성한다.
     */
    public String generateRefreshToken(String email) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.refreshTokenExpirationDays(), ChronoUnit.DAYS);
        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(email)
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰을 검증하고 subject를 반환한다.
     */
    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰이 유효한지 검증한다.
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * refresh token 만료 시각을 반환한다.
     */
    public Instant extractExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
