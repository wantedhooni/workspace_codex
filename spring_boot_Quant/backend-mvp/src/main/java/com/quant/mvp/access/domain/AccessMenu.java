package com.quant.mvp.access.domain;

import com.quant.mvp.common.jpa.BaseAuditUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "menus")
public class AccessMenu extends BaseAuditUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_menu_id")
    private Long parentMenuId;

    @Column(name = "menu_key", nullable = false, unique = true, length = 80)
    private String menuKey;

    @Column(name = "menu_label", nullable = false, length = 120)
    private String menuLabel;

    @Column(name = "path", nullable = false, length = 255)
    private String path;

    @Column(name = "icon", nullable = false, length = 80)
    private String icon;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    protected AccessMenu() {
    }

    private AccessMenu(
            Long parentMenuId,
            String menuKey,
            String menuLabel,
            String path,
            String icon,
            Integer sortOrder,
            boolean enabled
    ) {
        this.parentMenuId = parentMenuId;
        this.menuKey = menuKey;
        this.menuLabel = menuLabel;
        this.path = path;
        this.icon = icon;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
    }

    public static AccessMenu create(
            Long parentMenuId,
            String menuKey,
            String menuLabel,
            String path,
            String icon,
            Integer sortOrder,
            boolean enabled
    ) {
        return new AccessMenu(parentMenuId, menuKey, menuLabel, path, icon, sortOrder, enabled);
    }

    public Long getId() {
        return id;
    }

    public Long getParentMenuId() {
        return parentMenuId;
    }

    public String getMenuKey() {
        return menuKey;
    }

    public String getMenuLabel() {
        return menuLabel;
    }

    public String getPath() {
        return path;
    }

    public String getIcon() {
        return icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
