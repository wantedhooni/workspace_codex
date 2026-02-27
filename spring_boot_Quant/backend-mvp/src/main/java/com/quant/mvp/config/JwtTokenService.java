package com.quant.mvp.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final String issuer;
    private final byte[] secret;
    private final long expirationSeconds;

    public JwtTokenService(
            @Value("${app.security.jwt.issuer}") String issuer,
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.issuer = issuer;
        this.secret = validateSecret(secret);
        this.expirationSeconds = expirationSeconds;
    }

    public TokenIssue issueToken(Long userId, String email, String name, List<String> roleCodes) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);

        String token = Jwts.builder()
                .issuer(issuer)
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim("uid", userId)
                .claim("name", name)
                .claim("roles", roleCodes)
                .signWith(Keys.hmacShaKeyFor(secret))
                .compact();

        return new TokenIssue(token, expirationSeconds, expiresAt);
    }

    public TokenClaims parseAndValidate(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String tokenIssuer = claims.getIssuer();
        if (tokenIssuer == null || !issuer.equals(tokenIssuer)) {
            throw new IllegalArgumentException("token issuer is invalid");
        }

        Long userId = parseLongClaim(claims.get("uid"), "uid");
        String email = claims.getSubject();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("token subject(email) is missing");
        }

        String name = String.valueOf(claims.getOrDefault("name", ""));
        List<String> roleCodes = parseRoleCodes(claims.get("roles"));
        return new TokenClaims(userId, email, name, roleCodes);
    }

    private byte[] validateSecret(String raw) {
        byte[] bytes = raw == null ? new byte[0] : raw.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("jwt secret must be at least 32 bytes");
        }
        return bytes;
    }

    private Long parseLongClaim(Object value, String claimName) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        throw new IllegalArgumentException("token claim missing or invalid: " + claimName);
    }

    private List<String> parseRoleCodes(Object value) {
        if (!(value instanceof List<?> rawList)) {
            return List.of();
        }

        List<String> roleCodes = new ArrayList<>();
        for (Object item : rawList) {
            if (item == null) {
                continue;
            }
            String code = item.toString().trim();
            if (!code.isEmpty()) {
                roleCodes.add(code);
            }
        }
        return roleCodes;
    }

    public record TokenIssue(String accessToken, long expiresInSeconds, Instant expiresAt) {
    }

    public record TokenClaims(Long userId, String email, String name, List<String> roleCodes) {
    }
}
