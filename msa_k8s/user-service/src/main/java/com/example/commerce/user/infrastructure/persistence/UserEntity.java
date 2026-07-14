package com.example.commerce.user.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.example.commerce.user.domain.User;
import com.example.commerce.user.domain.UserStatus;

/**
 * PostgreSQL의 commerce_user 테이블과 회원 도메인 모델을 연결하는 JPA 엔티티다.
 */
@Entity
@Table(name = "commerce_user")
public class UserEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * JPA가 엔티티를 복원할 때 사용하는 생성자다.
     */
    protected UserEntity() {
    }

    private UserEntity(UUID id, String email, String name, UserStatus status, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * 회원 도메인 모델을 영속 엔티티로 변환한다.
     *
     * @param user 변환할 회원
     * @return 회원 영속 엔티티
     */
    public static UserEntity from(User user) {
        return new UserEntity(user.id(), user.email(), user.name(), user.status(), user.createdAt());
    }

    /**
     * 영속 엔티티를 회원 도메인 모델로 변환한다.
     *
     * @return 회원 도메인 모델
     */
    public User toDomain() {
        return new User(id, email, name, status, createdAt);
    }
}

