package com.portal.admin.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "access_logs")
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private Boolean success;

    @Column(name = "logged_at", nullable = false)
    private Instant loggedAt;

    protected AccessLog() {}

    public AccessLog(String username, String ipAddress, String action, String path, Boolean success) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.action = action;
        this.path = path;
        this.success = success;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getIpAddress() { return ipAddress; }
    public String getAction() { return action; }
    public String getPath() { return path; }
    public Boolean getSuccess() { return success; }
    public Instant getLoggedAt() { return loggedAt; }

    public void setUsername(String username) { this.username = username; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setAction(String action) { this.action = action; }
    public void setPath(String path) { this.path = path; }
    public void setSuccess(Boolean success) { this.success = success; }

    @PrePersist
    void onCreate() {
        if (loggedAt == null) {
            loggedAt = Instant.now();
        }
    }
}
