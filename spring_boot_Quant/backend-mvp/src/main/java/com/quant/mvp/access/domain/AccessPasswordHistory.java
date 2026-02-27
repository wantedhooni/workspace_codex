package com.quant.mvp.access.domain;

import com.quant.mvp.common.jpa.BaseAuditUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_password_histories")
public class AccessPasswordHistory extends BaseAuditUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    protected AccessPasswordHistory() {
    }

    private AccessPasswordHistory(Long userId, String passwordHash) {
        this.userId = userId;
        this.passwordHash = passwordHash;
    }

    public static AccessPasswordHistory create(Long userId, String passwordHash) {
        return new AccessPasswordHistory(userId, passwordHash);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
