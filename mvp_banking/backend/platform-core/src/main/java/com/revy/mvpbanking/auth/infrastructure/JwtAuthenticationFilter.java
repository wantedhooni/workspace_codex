package com.revy.mvpbanking.auth.infrastructure;

import com.revy.mvpbanking.auth.domain.AuthenticatedPrincipal;
import com.revy.mvpbanking.auth.domain.AuthenticatedPrincipalLoader;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticatedPrincipalLoader principalLoader;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            AuthenticatedPrincipalLoader principalLoader
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.principalLoader = principalLoader;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            resolveBearerToken(request)
                    .map(jwtTokenProvider::parse)
                    .flatMap(payload -> principalLoader.load(payload.principalType(), payload.principalId()))
                    .ifPresent(this::authenticate);
        } catch (JwtException ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return Optional.empty();
        }
        return Optional.of(header.substring(7));
    }

    private void authenticate(AuthenticatedPrincipal principal) {
        var userPrincipal = new AuthUserPrincipal(principal);
        var authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
