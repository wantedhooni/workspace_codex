package com.portal.admin.domain;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "admin_users")
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(name = "token_version")
    private Integer tokenVersion = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "admin_user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    protected AdminUser() {}

    public AdminUser(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Integer getTokenVersion() { return tokenVersion; }
    public Set<Role> getRoles() { return roles; }

    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setTokenVersion(Integer tokenVersion) { this.tokenVersion = tokenVersion == null ? 0 : tokenVersion; }
    public void incrementTokenVersion() {
        this.tokenVersion = this.tokenVersion == null ? 1 : this.tokenVersion + 1;
    }
    public void setRoles(Set<Role> roles) {
        this.roles = roles == null ? new HashSet<>() : new HashSet<>(roles);
    }

    @PrePersist
    void onCreate() {
        if (tokenVersion == null) {
            tokenVersion = 0;
        }
    }
}
