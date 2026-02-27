package com.commerce.service_auth.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.issuer:commerce-platform}")
    private String issuer;

    @Value("${security.jwt.ttl-seconds:7200}")
    private long ttlSeconds;

    public String issueToken(String subject, String tenantId, List<String> roles, List<String> scopes) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds);

        return Jwts.builder()
            .setSubject(subject)
            .setIssuer(issuer)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(exp))
            .addClaims(Map.of(
                "tenant_id", tenantId,
                "roles", roles,
                "scopes", scopes
            ))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }
}
