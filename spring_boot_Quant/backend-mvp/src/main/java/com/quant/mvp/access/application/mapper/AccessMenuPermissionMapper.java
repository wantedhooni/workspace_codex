package com.quant.mvp.access.application.mapper;

import com.quant.mvp.access.domain.AccessMenuPermission;
import com.quant.mvp.pipeline.domain.MenuPermission;

public final class AccessMenuPermissionMapper {

    private AccessMenuPermissionMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static MenuPermission toDomain(AccessMenuPermission permission) {
        if (permission == null) {
            return null;
        }
        return new MenuPermission(
                permission.getId(),
                permission.getMenuId(),
                permission.getRoleId(),
                permission.isCanRead(),
                permission.isCanCreate(),
                permission.isCanUpdate(),
                permission.isCanDelete(),
                permission.getUpdatedAt()
        );
    }
}
