package com.quant.mvp.access.domain;

import com.quant.mvp.common.jpa.BaseAuditUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "account_sessions")
public class AccessAccountSession extends BaseAuditUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "ip_address", nullable = false, length = 80)
    private String ipAddress;

    @Column(name = "user_agent", nullable = false, length = 300)
    private String userAgent;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "last_access_at", nullable = false)
    private Instant lastAccessAt;

    protected AccessAccountSession() {
    }

    private AccessAccountSession(Long userId, String ipAddress, String userAgent, boolean active, Instant lastAccessAt) {
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.active = active;
        this.lastAccessAt = lastAccessAt;
    }

    public static AccessAccountSession create(
            Long userId,
            String ipAddress,
            String userAgent,
            boolean active,
            Instant lastAccessAt
    ) {
        return new AccessAccountSession(userId, ipAddress, userAgent, active, lastAccessAt);
    }

    public void revoke(Instant now) {
        this.active = false;
        this.lastAccessAt = now;
    }

    public void touch(Instant now) {
        this.lastAccessAt = now;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getLastAccessAt() {
        return lastAccessAt;
    }
}
