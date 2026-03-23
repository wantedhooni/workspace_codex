package com.example.samplegoogleoauth.auth.service;

import com.example.samplegoogleoauth.auth.entity.OAuthMember;
import com.example.samplegoogleoauth.auth.repository.OAuthMemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth 로그인 직후 회원 기본 정보를 RDBMS에 동기화한다.
 */
@Service
public class OAuthMemberSyncService {

    private final OAuthMemberRepository oauthMemberRepository;
    private final OAuthPrincipalResolver oauthPrincipalResolver;

    public OAuthMemberSyncService(
        OAuthMemberRepository oauthMemberRepository,
        OAuthPrincipalResolver oauthPrincipalResolver
    ) {
        this.oauthMemberRepository = oauthMemberRepository;
        this.oauthPrincipalResolver = oauthPrincipalResolver;
    }

    /**
     * OAuth 로그인 성공 시 사용자 기본 계정을 생성하거나 최신 프로필로 갱신한다.
     */
    @Transactional
    public OAuthMember syncOnLogin(Authentication authentication) {
        OAuthPrincipalInfo principalInfo = oauthPrincipalResolver.resolve(authentication);

        OAuthMember member = oauthMemberRepository
            .findByProviderAndProviderUserId(principalInfo.provider(), principalInfo.providerUserId())
            .map(existing -> {
                existing.syncOAuthProfile(
                    principalInfo.email(),
                    principalInfo.name(),
                    principalInfo.picture()
                );
                return existing;
            })
            .orElseGet(() -> new OAuthMember(
                principalInfo.provider(),
                principalInfo.providerUserId(),
                principalInfo.email(),
                principalInfo.name(),
                principalInfo.picture()
            ));

        return oauthMemberRepository.save(member);
    }
}
