package com.example.marketsignal.user;

import com.example.marketsignal.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 플랫폼 사용자를 저장하는 엔티티다.
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 500)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(length = 600)
    private String refreshToken;

    private Instant refreshTokenExpiresAt;

    @Builder
    public User(String email, String password, String name, String bio, UserRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.bio = bio;
        this.role = role;
    }

    /**
     * 사용자 프로필 정보를 수정한다.
     */
    public void updateProfile(String name, String bio) {
        this.name = name;
        this.bio = bio;
    }

    /**
     * refresh token 정보를 갱신한다.
     */
    public void updateRefreshToken(String refreshToken, Instant expiresAt) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = expiresAt;
    }

    /**
     * refresh token 정보를 제거한다.
     */
    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiresAt = null;
    }
}
