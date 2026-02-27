package com.portal.admin.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "service_audit_logs")
public class ServiceAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "domain_type", nullable = false)
    private String domainType;

    @Column(name = "domain_id", nullable = false)
    private Long domainId;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String username;

    @Column(length = 4000)
    private String detail;

    @Column(name = "logged_at", nullable = false)
    private Instant loggedAt;

    protected ServiceAuditLog() {}

    public ServiceAuditLog(String domainType, Long domainId, String action, String username, String detail) {
        this.domainType = domainType;
        this.domainId = domainId;
        this.action = action;
        this.username = username;
        this.detail = detail;
    }

    public Long getId() { return id; }
    public String getDomainType() { return domainType; }
    public Long getDomainId() { return domainId; }
    public String getAction() { return action; }
    public String getUsername() { return username; }
    public String getDetail() { return detail; }
    public Instant getLoggedAt() { return loggedAt; }

    public void setDomainType(String domainType) { this.domainType = domainType; }
    public void setDomainId(Long domainId) { this.domainId = domainId; }
    public void setAction(String action) { this.action = action; }
    public void setUsername(String username) { this.username = username; }
    public void setDetail(String detail) { this.detail = detail; }

    @PrePersist
    void onCreate() {
        if (loggedAt == null) {
            loggedAt = Instant.now();
        }
    }
}
