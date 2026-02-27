package com.commerce.service_auth.user;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String tenantId;

    protected User() {}

    public User(String username, String passwordHash, Role role, String tenantId) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.tenantId = tenantId;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public String getTenantId() { return tenantId; }
}
