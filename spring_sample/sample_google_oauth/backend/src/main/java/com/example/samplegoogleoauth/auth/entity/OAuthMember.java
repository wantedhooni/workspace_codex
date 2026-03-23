package com.example.samplegoogleoauth.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * OAuth 로그인 사용자의 기본 계정 정보와 가입 완료 상태를 RDBMS에 저장한다.
 */
@Entity
@Table(name = "oauth_member")
public class OAuthMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String provider;

    @Column(nullable = false, unique = true, length = 120)
    private String providerUserId;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 120)
    private String oauthName;

    @Column(length = 255)
    private String pictureUrl;

    @Column(nullable = false)
    private boolean registered;

    @Column(length = 50)
    private String displayName;

    @Column(length = 80)
    private String organization;

    @Column(length = 60)
    private String jobTitle;

    @Column(nullable = false)
    private boolean marketingConsent;

    @Column(nullable = false)
    private LocalDateTime lastLoginAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected OAuthMember() {
    }

    public OAuthMember(
        String provider,
        String providerUserId,
        String email,
        String oauthName,
        String pictureUrl
    ) {
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.email = email;
        this.oauthName = oauthName;
        this.pictureUrl = pictureUrl;
        this.registered = false;
        this.displayName = null;
        this.organization = null;
        this.jobTitle = null;
        this.marketingConsent = false;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.lastLoginAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void syncOAuthProfile(String email, String oauthName, String pictureUrl) {
        this.email = email;
        this.oauthName = oauthName;
        this.pictureUrl = pictureUrl;
        this.lastLoginAt = LocalDateTime.now();
    }

    public void completeRegistration(String displayName, String organization, String jobTitle, boolean marketingConsent) {
        this.displayName = displayName;
        this.organization = organization;
        this.jobTitle = jobTitle;
        this.marketingConsent = marketingConsent;
        this.registered = true;
    }

    public Long getId() {
        return id;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public String getEmail() {
        return email;
    }

    public String getOauthName() {
        return oauthName;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public boolean isRegistered() {
        return registered;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getOrganization() {
        return organization;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public boolean isMarketingConsent() {
        return marketingConsent;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
}
