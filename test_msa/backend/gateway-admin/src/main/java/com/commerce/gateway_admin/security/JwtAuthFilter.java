package com.commerce.gateway_admin.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.issuer:commerce-platform}")
    private String issuer;

    @Value("${security.jwt.allowed-roles:}")
    private List<String> allowedRoles;

    @Value("${security.jwt.public-paths:/auth/**,/actuator/health}")
    private List<String> publicPaths;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = auth.substring(7);
        Claims claims;
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            claims = Jwts.parserBuilder().setSigningKey(key).requireIssuer(issuer).build().parseClaimsJws(token).getBody();
        } catch (JwtException ex) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        List<String> roles = asStringList(claims.get("roles"));
        if (!allowedRoles.isEmpty() && roles.stream().noneMatch(allowedRoles::contains)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        String userId = claims.getSubject();
        String tenantId = claims.get("tenant_id", String.class);
        List<String> scopes = asStringList(claims.get("scopes"));

        ServerWebExchange mutated = exchange.mutate().request(builder -> builder
            .header("X-User-Id", userId == null ? "" : userId)
            .header("X-Tenant-Id", tenantId == null ? "" : tenantId)
            .header("X-Roles", String.join(",", roles))
            .header("X-Scopes", String.join(",", scopes))
        ).build();

        return chain.filter(mutated);
    }

    private boolean isPublic(String path) {
        for (String pattern : publicPaths) {
            if (pathMatcher.match(pattern.trim(), path)) {
                return true;
            }
        }
        return false;
    }

    private List<String> asStringList(Object raw) {
        if (raw == null) return List.of();
        if (raw instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object o : list) out.add(String.valueOf(o));
            return out;
        }
        return List.of(String.valueOf(raw));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
