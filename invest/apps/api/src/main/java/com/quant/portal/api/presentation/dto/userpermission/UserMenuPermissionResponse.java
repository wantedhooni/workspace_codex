package com.quant.portal.api.presentation.dto.userpermission;

public record UserMenuPermissionResponse(
        String menuKey,
        boolean canList,
        boolean canCreate,
        boolean canEdit,
        boolean canDelete
) {
}
