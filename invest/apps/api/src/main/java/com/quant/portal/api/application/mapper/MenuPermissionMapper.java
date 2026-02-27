package com.quant.portal.api.application.mapper;

import com.quant.portal.api.presentation.dto.menupermission.MenuPermissionCreateRequest;
import com.quant.portal.api.presentation.dto.menupermission.MenuPermissionResponse;
import com.quant.portal.api.presentation.dto.userpermission.UserMenuPermissionResponse;
import com.quant.portal.domain.admin.entity.MenuPermission;

public final class MenuPermissionMapper {

    private MenuPermissionMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static MenuPermission toEntity(MenuPermissionCreateRequest request) {
        if (request == null) {
            return null;
        }

        return new MenuPermission(
                request.roleCode(),
                request.menuKey(),
                request.canList(),
                request.canCreate(),
                request.canEdit(),
                request.canDelete()
        );
    }

    public static MenuPermissionResponse toDto(MenuPermission permission) {
        if (permission == null) {
            return null;
        }

        return new MenuPermissionResponse(
                permission.getId(),
                permission.getRoleCode(),
                permission.getMenuKey(),
                permission.isCanList(),
                permission.isCanCreate(),
                permission.isCanEdit(),
                permission.isCanDelete()
        );
    }

    public static UserMenuPermissionResponse toUserDto(MenuPermission permission) {
        if (permission == null) {
            return null;
        }

        return new UserMenuPermissionResponse(
                permission.getMenuKey(),
                permission.isCanList(),
                permission.isCanCreate(),
                permission.isCanEdit(),
                permission.isCanDelete()
        );
    }
}
