package com.quant.portal.domain.admin.entity;

import com.quant.portal.common.jpa.BaseAuditUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(
        name = "menu_permissions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_menu_permissions_role_menu", columnNames = {"role_code", "menu_key"})
        }
)
public class MenuPermission extends BaseAuditUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_code", nullable = false, length = 50)
    private String roleCode;

    @Column(name = "menu_key", nullable = false, length = 100)
    private String menuKey;

    @Column(name = "can_list", nullable = false)
    private boolean canList;

    @Column(name = "can_create", nullable = false)
    private boolean canCreate;

    @Column(name = "can_edit", nullable = false)
    private boolean canEdit;

    @Column(name = "can_delete", nullable = false)
    private boolean canDelete;

    protected MenuPermission() {
    }

    public MenuPermission(
            String roleCode,
            String menuKey,
            boolean canList,
            boolean canCreate,
            boolean canEdit,
            boolean canDelete
    ) {
        this.roleCode = normalizeRoleCode(roleCode);
        this.menuKey = normalizeMenuKey(menuKey);
        this.canList = canList;
        this.canCreate = canCreate;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
    }

    public Long getId() {
        return id;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getMenuKey() {
        return menuKey;
    }

    public boolean isCanList() {
        return canList;
    }

    public boolean isCanCreate() {
        return canCreate;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void update(
            String roleCode,
            String menuKey,
            boolean canList,
            boolean canCreate,
            boolean canEdit,
            boolean canDelete
    ) {
        this.roleCode = normalizeRoleCode(roleCode);
        this.menuKey = normalizeMenuKey(menuKey);
        this.canList = canList;
        this.canCreate = canCreate;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
    }

    private static String normalizeRoleCode(String value) {
        String roleCode = requireText(value, "roleCode");
        return roleCode.toUpperCase(Locale.ROOT);
    }

    private static String normalizeMenuKey(String value) {
        String menuKey = requireText(value, "menuKey");
        return menuKey.toLowerCase(Locale.ROOT);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
