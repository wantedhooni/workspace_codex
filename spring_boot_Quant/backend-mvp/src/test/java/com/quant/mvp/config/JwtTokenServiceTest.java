package com.quant.mvp.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    private static final String SECRET = "test-jwt-secret-key-for-unit-tests-2026-very-long";

    @Test
    void parseRejectsMismatchedIssuer() {
        JwtTokenService service = new JwtTokenService("quant-backend-mvp", SECRET, 3600);

        String foreignIssuerToken = Jwts.builder()
                .issuer("foreign-issuer")
                .subject("admin@quant.io")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .claim("uid", 1L)
                .claim("name", "Admin")
                .claim("roles", List.of("ADMIN"))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThrows(IllegalArgumentException.class, () -> service.parseAndValidate(foreignIssuerToken));
    }

    @Test
    void issueAndParseKeepsClaims() {
        JwtTokenService service = new JwtTokenService("quant-backend-mvp", SECRET, 3600);
        JwtTokenService.TokenIssue tokenIssue = service.issueToken(1L, "admin@quant.io", "Admin", List.of("ADMIN"));

        JwtTokenService.TokenClaims claims = service.parseAndValidate(tokenIssue.accessToken());
        assertEquals(1L, claims.userId());
        assertEquals("admin@quant.io", claims.email());
        assertEquals(List.of("ADMIN"), claims.roleCodes());
    }
}
