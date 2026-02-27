package com.quant.mvp.access.domain;

import com.quant.mvp.common.jpa.BaseAuditUserEntity;
import com.quant.mvp.pipeline.domain.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "users")
public class AccessUser extends BaseAuditUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    protected AccessUser() {
    }

    private AccessUser(String email, String name, UserStatus status, String passwordHash) {
        this.email = email;
        this.name = name;
        this.status = status;
        this.passwordHash = passwordHash;
    }

    public static AccessUser create(String email, String name, UserStatus status, String passwordHash) {
        return new AccessUser(email, name, status, passwordHash);
    }

    public void updateStatus(UserStatus nextStatus) {
        this.status = nextStatus;
    }

    public void updateLastLoginAt(Instant nextLastLoginAt) {
        this.lastLoginAt = nextLastLoginAt;
    }

    public void updatePasswordHash(String nextPasswordHash) {
        this.passwordHash = nextPasswordHash;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
