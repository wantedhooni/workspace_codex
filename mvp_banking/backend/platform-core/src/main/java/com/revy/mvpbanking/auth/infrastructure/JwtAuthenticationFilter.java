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

/**
 * 요청 헤더의 Bearer JWT를 해석해 Spring Security 인증 컨텍스트를 구성하는 필터입니다.
 * <p>
 * 토큰이 없거나 유효하지 않은 경우에는 인증 정보를 비우고 다음 필터로 요청을 전달합니다.
 */
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

    /**
     * JWT를 검증하고 사용자 정보를 SecurityContext에 주입합니다.
     */
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

    /**
     * Authorization 헤더에서 Bearer 토큰을 추출합니다.
     */
    private Optional<String> resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return Optional.empty();
        }
        return Optional.of(header.substring(7));
    }

    /**
     * 인증된 사용자 정보를 Spring Security 인증 객체로 변환해 컨텍스트에 저장합니다.
     */
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
