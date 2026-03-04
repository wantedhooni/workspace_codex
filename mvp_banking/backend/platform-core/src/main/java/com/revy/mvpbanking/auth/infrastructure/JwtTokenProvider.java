package com.revy.mvpbanking.auth.infrastructure;

import com.revy.mvpbanking.auth.domain.AuthenticatedPrincipal;
import com.revy.mvpbanking.auth.domain.PrincipalType;
import com.revy.mvpbanking.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(AuthenticatedPrincipal principal) {
        var now = Instant.now();
        var expiresAt = now.plusSeconds(jwtProperties.accessTokenExpirationSeconds());

        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(principal.email())
                .claim("pid", principal.id().toString())
                .claim("ptype", principal.principalType().name())
                .claim("name", principal.displayName())
                .claim("roles", principal.roles())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public JwtAuthenticatedPayload parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        return new JwtAuthenticatedPayload(
                UUID.fromString(claims.get("pid", String.class)),
                claims.getSubject(),
                claims.get("name", String.class),
                PrincipalType.valueOf(claims.get("ptype", String.class)),
                roles == null ? List.of() : roles
        );
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.accessTokenExpirationSeconds();
    }

    public long getRefreshTokenExpirationSeconds() {
        return jwtProperties.refreshTokenExpirationSeconds();
    }
}
