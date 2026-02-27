package com.quant.portal.api.presentation.dto.menupermission;

public record MenuPermissionResponse(
        Long id,
        String roleCode,
        String menuKey,
        boolean canList,
        boolean canCreate,
        boolean canEdit,
        boolean canDelete
) {
}
