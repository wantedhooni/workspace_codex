package com.example.websample.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** 요청의 Bearer 토큰을 검증하고 주입된 로더로 인증 주체를 복원하는 필터입니다. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtPrincipalResolver jwtPrincipalResolver;
    private final JwtTokenBlacklist jwtTokenBlacklist;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            JwtPrincipalResolver jwtPrincipalResolver,
            JwtTokenBlacklist jwtTokenBlacklist
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtPrincipalResolver = jwtPrincipalResolver;
        this.jwtTokenBlacklist = jwtTokenBlacklist;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            authenticate(authorization.substring(BEARER_PREFIX.length()));
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String token) {
        try {
            Claims claims = jwtTokenProvider.parseClaims(token);
            if (jwtTokenProvider.getTokenType(claims) != JwtTokenType.ACCESS) {
                return;
            }
            String tokenId = jwtTokenProvider.getTokenId(claims);
            if (jwtTokenBlacklist.contains(tokenId)) {
                return;
            }
            Long principalId = Long.valueOf(claims.getSubject());
            String principalType = claims.get(JwtTokenProvider.PRINCIPAL_TYPE_CLAIM, String.class);
            jwtPrincipalResolver.resolve(principalType, principalId).ifPresent(this::setAuthentication);
        } catch (JwtException | IllegalArgumentException ignored) {
            SecurityContextHolder.clearContext();
        }
    }

    private void setAuthentication(JwtPrincipal principal) {
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + principal.role())
        );
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
