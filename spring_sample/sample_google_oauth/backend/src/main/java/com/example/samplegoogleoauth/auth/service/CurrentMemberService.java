package com.example.samplegoogleoauth.auth.service;

import com.example.samplegoogleoauth.auth.entity.OAuthMember;
import com.example.samplegoogleoauth.auth.repository.OAuthMemberRepository;
import com.example.samplegoogleoauth.auth.security.AuthenticatedMemberPrincipal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * JWT 인증 이후 현재 요청 회원을 조회한다.
 */
@Service
public class CurrentMemberService {

    private final OAuthMemberRepository oauthMemberRepository;

    public CurrentMemberService(OAuthMemberRepository oauthMemberRepository) {
        this.oauthMemberRepository = oauthMemberRepository;
    }

    /**
     * SecurityContext에 저장된 회원 식별자를 기준으로 현재 회원 엔티티를 조회한다.
     */
    public OAuthMember getCurrentMember(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("인증된 사용자만 처리할 수 있습니다.");
        }

        if (!(authentication.getPrincipal() instanceof AuthenticatedMemberPrincipal principal)) {
            throw new IllegalStateException("JWT 인증 정보가 아닙니다.");
        }

        return oauthMemberRepository.findById(principal.memberId())
            .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));
    }
}
