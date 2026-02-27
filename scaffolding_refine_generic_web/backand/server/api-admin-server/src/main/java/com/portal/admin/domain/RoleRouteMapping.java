package com.portal.admin.domain;

import jakarta.persistence.*;

@Entity
@Table(
        name = "role_route_mappings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "pattern", "http_method"})
)
public class RoleRouteMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(nullable = false)
    private String pattern;

    @Column(name = "http_method", nullable = false)
    private String httpMethod;

    private String description;

    @Column(nullable = false)
    private Boolean enabled;

    protected RoleRouteMapping() {}

    public RoleRouteMapping(Long roleId, String pattern, String httpMethod, String description, Boolean enabled) {
        this.roleId = roleId;
        this.pattern = pattern;
        this.httpMethod = httpMethod;
        this.description = description;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public Long getRoleId() { return roleId; }
    public String getPattern() { return pattern; }
    public String getHttpMethod() { return httpMethod; }
    public String getDescription() { return description; }
    public Boolean getEnabled() { return enabled; }

    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public void setPattern(String pattern) { this.pattern = pattern; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public void setDescription(String description) { this.description = description; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
