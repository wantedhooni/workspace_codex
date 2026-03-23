package com.example.samplegoogleoauth.auth.service;

import com.example.samplegoogleoauth.auth.dto.SignupRequest;
import com.example.samplegoogleoauth.auth.dto.UserProfileResponse;
import com.example.samplegoogleoauth.auth.entity.OAuthMember;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth 사용자의 서비스 회원가입과 프로필 조회를 담당한다.
 */
@Service
public class OAuthSignupService {

    private final CurrentMemberService currentMemberService;

    public OAuthSignupService(CurrentMemberService currentMemberService) {
        this.currentMemberService = currentMemberService;
    }

    /**
     * 현재 로그인한 사용자의 가입 여부를 포함한 프로필 정보를 반환한다.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(Authentication authentication) {
        OAuthMember member = currentMemberService.getCurrentMember(authentication);
        return toUserProfileResponse(member);
    }

    /**
     * OAuth 인증이 끝난 사용자의 추가 프로필 정보를 저장해 회원가입을 완료한다.
     */
    @Transactional
    public UserProfileResponse signup(Authentication authentication, SignupRequest request) {
        OAuthMember member = currentMemberService.getCurrentMember(authentication);

        if (member.isRegistered()) {
            throw new IllegalStateException("이미 가입이 완료된 사용자입니다.");
        }

        member.completeRegistration(
            request.displayName().trim(),
            request.organization().trim(),
            request.jobTitle().trim(),
            request.marketingConsent()
        );

        return toUserProfileResponse(member);
    }

    private UserProfileResponse toUserProfileResponse(OAuthMember member) {
        return new UserProfileResponse(
            true,
            member.isRegistered(),
            member.getOauthName(),
            member.getEmail(),
            member.getPictureUrl(),
            member.getProvider(),
            member.isRegistered() ? member.getDisplayName() : member.getOauthName(),
            member.getOrganization() == null ? "" : member.getOrganization(),
            member.getJobTitle() == null ? "" : member.getJobTitle(),
            member.isMarketingConsent()
        );
    }
}
