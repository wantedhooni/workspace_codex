package com.portal.admin.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "login_policies")
public class LoginPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "max_fail_count", nullable = false)
    private Integer maxFailCount;

    @Column(name = "lock_minutes", nullable = false)
    private Integer lockMinutes;

    @Column(name = "allowed_ip_cidr")
    private String allowedIpCidr;

    @Column(nullable = false)
    private Boolean enabled;

    protected LoginPolicy() {}

    public LoginPolicy(String name, Integer maxFailCount, Integer lockMinutes, String allowedIpCidr, Boolean enabled) {
        this.name = name;
        this.maxFailCount = maxFailCount;
        this.lockMinutes = lockMinutes;
        this.allowedIpCidr = allowedIpCidr;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getMaxFailCount() { return maxFailCount; }
    public Integer getLockMinutes() { return lockMinutes; }
    public String getAllowedIpCidr() { return allowedIpCidr; }
    public Boolean getEnabled() { return enabled; }

    public void setName(String name) { this.name = name; }
    public void setMaxFailCount(Integer maxFailCount) { this.maxFailCount = maxFailCount; }
    public void setLockMinutes(Integer lockMinutes) { this.lockMinutes = lockMinutes; }
    public void setAllowedIpCidr(String allowedIpCidr) { this.allowedIpCidr = allowedIpCidr; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
