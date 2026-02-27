package com.quant.portal.api.application.mapper;

import com.quant.portal.api.presentation.dto.userpermission.UserMenuPermissionResponse;

public final class UserMenuPermissionMapper {

    private UserMenuPermissionMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static UserMenuPermissionResponse merge(
            UserMenuPermissionResponse current,
            UserMenuPermissionResponse incoming
    ) {
        if (current == null) {
            return incoming;
        }
        return new UserMenuPermissionResponse(
                current.menuKey(),
                current.canList() || incoming.canList(),
                current.canCreate() || incoming.canCreate(),
                current.canEdit() || incoming.canEdit(),
                current.canDelete() || incoming.canDelete()
        );
    }
}
