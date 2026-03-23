package com.example.samplegoogleoauth.auth.security;

import com.example.samplegoogleoauth.auth.entity.OAuthMember;
import com.example.samplegoogleoauth.auth.repository.OAuthMemberRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Bearer access token을 해석해 SecurityContext를 구성한다.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthMemberRepository oauthMemberRepository;

    public JwtAuthenticationFilter(
        JwtTokenProvider jwtTokenProvider,
        OAuthMemberRepository oauthMemberRepository
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.oauthMemberRepository = oauthMemberRepository;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);

            try {
                Claims claims = jwtTokenProvider.parseAccessToken(token);
                Long memberId = jwtTokenProvider.extractMemberId(claims);
                OAuthMember member = oauthMemberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));

                AuthenticatedMemberPrincipal principal = new AuthenticatedMemberPrincipal(
                    member.getId(),
                    member.getProvider(),
                    member.getProviderUserId(),
                    member.getEmail()
                );

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    token,
                    List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (IllegalStateException exception) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
