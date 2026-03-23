package com.example.samplegoogleoauth.auth.repository;

import com.example.samplegoogleoauth.auth.entity.OAuthMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * OAuth 가입 사용자 프로필을 조회하고 저장한다.
 */
public interface OAuthMemberRepository extends JpaRepository<OAuthMember, Long> {

    Optional<OAuthMember> findByProviderAndProviderUserId(String provider, String providerUserId);
}
