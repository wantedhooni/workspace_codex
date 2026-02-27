package com.quant.portal.domain.admin.entity;

import com.quant.portal.common.jpa.BaseAuditUserEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "admin_users")
public class AdminUser extends BaseAuditUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "admin_user_roles",
            joinColumns = @JoinColumn(name = "admin_user_id"),
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_admin_user_roles_user_role",
                            columnNames = {"admin_user_id", "role_code"}
                    )
            }
    )
    @Column(name = "role_code", nullable = false, length = 50)
    private Set<String> roleCodes = new LinkedHashSet<>();

    protected AdminUser() {
    }

    public AdminUser(
            String username,
            String passwordHash,
            String displayName,
            boolean enabled,
            Set<String> roleCodes
    ) {
        this.username = normalizeUsername(username);
        this.passwordHash = requireText(passwordHash, "passwordHash");
        this.displayName = requireText(displayName, "displayName");
        this.enabled = enabled;
        this.roleCodes = normalizeRoleCodes(roleCodes);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<String> getRoleCodes() {
        return Set.copyOf(roleCodes);
    }

    public void updateProfile(String displayName, boolean enabled) {
        this.displayName = requireText(displayName, "displayName");
        this.enabled = enabled;
    }

    public void updateRoles(Set<String> roleCodes) {
        this.roleCodes = normalizeRoleCodes(roleCodes);
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = requireText(passwordHash, "passwordHash");
    }

    private static String normalizeUsername(String value) {
        return requireText(value, "username").toLowerCase(Locale.ROOT);
    }

    private static Set<String> normalizeRoleCodes(Set<String> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("roleCodes must not be empty");
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String roleCode : values) {
            normalized.add(requireText(roleCode, "roleCodes").toUpperCase(Locale.ROOT));
        }
        return normalized;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}

