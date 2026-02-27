package com.quant.mvp.access.domain;

import com.quant.mvp.common.jpa.BaseAuditUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class AccessRole extends BaseAuditUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_code", nullable = false, unique = true, length = 40)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 80)
    private String roleName;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "system_role", nullable = false)
    private boolean systemRole;

    protected AccessRole() {
    }

    private AccessRole(String roleCode, String roleName, String description, boolean systemRole) {
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.description = description;
        this.systemRole = systemRole;
    }

    public static AccessRole create(String roleCode, String roleName, String description, boolean systemRole) {
        return new AccessRole(roleCode, roleName, description, systemRole);
    }

    public Long getId() {
        return id;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSystemRole() {
        return systemRole;
    }
}
