package com.quant.mvp.access.domain;

import com.quant.mvp.common.jpa.BaseAuditUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "menu_permissions")
public class AccessMenuPermission extends BaseAuditUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "can_read", nullable = false)
    private boolean canRead;

    @Column(name = "can_create", nullable = false)
    private boolean canCreate;

    @Column(name = "can_update", nullable = false)
    private boolean canUpdate;

    @Column(name = "can_delete", nullable = false)
    private boolean canDelete;

    protected AccessMenuPermission() {
    }

    private AccessMenuPermission(
            Long menuId,
            Long roleId,
            boolean canRead,
            boolean canCreate,
            boolean canUpdate,
            boolean canDelete
    ) {
        this.menuId = menuId;
        this.roleId = roleId;
        this.canRead = canRead;
        this.canCreate = canCreate;
        this.canUpdate = canUpdate;
        this.canDelete = canDelete;
    }

    public static AccessMenuPermission create(
            Long menuId,
            Long roleId,
            boolean canRead,
            boolean canCreate,
            boolean canUpdate,
            boolean canDelete
    ) {
        return new AccessMenuPermission(menuId, roleId, canRead, canCreate, canUpdate, canDelete);
    }

    public void update(boolean canRead, boolean canCreate, boolean canUpdate, boolean canDelete) {
        this.canRead = canRead;
        this.canCreate = canCreate;
        this.canUpdate = canUpdate;
        this.canDelete = canDelete;
    }

    public Long getId() {
        return id;
    }

    public Long getMenuId() {
        return menuId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public boolean isCanRead() {
        return canRead;
    }

    public boolean isCanCreate() {
        return canCreate;
    }

    public boolean isCanUpdate() {
        return canUpdate;
    }

    public boolean isCanDelete() {
        return canDelete;
    }
}
