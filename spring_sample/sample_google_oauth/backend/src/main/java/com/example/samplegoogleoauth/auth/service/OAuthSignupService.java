package com.example.samplegoogleoauth.auth.service;

import com.example.samplegoogleoauth.auth.dto.SignupRequest;
import com.example.samplegoogleoauth.auth.dto.UserProfileResponse;
import com.example.samplegoogleoauth.auth.entity.OAuthMember;
import com.example.samplegoogleoauth.auth.repository.OAuthMemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth 사용자의 서비스 회원가입과 프로필 조회를 담당한다.
 */
@Service
public class OAuthSignupService {

    private final OAuthMemberRepository oauthMemberRepository;
    private final OAuthPrincipalResolver oauthPrincipalResolver;

    public OAuthSignupService(
        OAuthMemberRepository oauthMemberRepository,
        OAuthPrincipalResolver oauthPrincipalResolver
    ) {
        this.oauthMemberRepository = oauthMemberRepository;
        this.oauthPrincipalResolver = oauthPrincipalResolver;
    }

    /**
     * 현재 로그인한 사용자의 가입 여부를 포함한 프로필 정보를 반환한다.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(Authentication authentication) {
        OAuthPrincipalInfo principalInfo = oauthPrincipalResolver.resolve(authentication);

        return oauthMemberRepository.findByProviderAndProviderUserId(principalInfo.provider(), principalInfo.providerUserId())
            .map(member -> new UserProfileResponse(
                true,
                member.isRegistered(),
                principalInfo.name(),
                principalInfo.email(),
                principalInfo.picture(),
                principalInfo.provider(),
                member.isRegistered() ? member.getDisplayName() : principalInfo.name(),
                member.getOrganization(),
                member.getJobTitle(),
                member.isMarketingConsent()
            ))
            .orElseGet(() -> new UserProfileResponse(
                true,
                false,
                principalInfo.name(),
                principalInfo.email(),
                principalInfo.picture(),
                principalInfo.provider(),
                principalInfo.name(),
                "",
                "",
                false
            ));
    }

    /**
     * OAuth 인증이 끝난 사용자의 추가 프로필 정보를 저장해 회원가입을 완료한다.
     */
    @Transactional
    public UserProfileResponse signup(Authentication authentication, SignupRequest request) {
        OAuthPrincipalInfo principalInfo = oauthPrincipalResolver.resolve(authentication);
        OAuthMember member = oauthMemberRepository.findByProviderAndProviderUserId(principalInfo.provider(), principalInfo.providerUserId())
            .orElseGet(() -> new OAuthMember(
                principalInfo.provider(),
                principalInfo.providerUserId(),
                principalInfo.email(),
                principalInfo.name(),
                principalInfo.picture()
            ));

        if (member.isRegistered()) {
            throw new IllegalStateException("이미 가입이 완료된 사용자입니다.");
        }

        member.syncOAuthProfile(
            principalInfo.email(),
            principalInfo.name(),
            principalInfo.picture()
        );
        member.completeRegistration(
            request.displayName().trim(),
            request.organization().trim(),
            request.jobTitle().trim(),
            request.marketingConsent()
        );

        OAuthMember savedMember = oauthMemberRepository.save(member);

        return new UserProfileResponse(
            true,
            savedMember.isRegistered(),
            savedMember.getOauthName(),
            savedMember.getEmail(),
            savedMember.getPictureUrl(),
            savedMember.getProvider(),
            savedMember.getDisplayName(),
            savedMember.getOrganization(),
            savedMember.getJobTitle(),
            savedMember.isMarketingConsent()
        );
    }
}
