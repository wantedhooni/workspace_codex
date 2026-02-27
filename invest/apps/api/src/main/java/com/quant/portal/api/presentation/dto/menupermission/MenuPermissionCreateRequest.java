package com.quant.portal.api.presentation.dto.menupermission;

import jakarta.validation.constraints.NotBlank;

public record MenuPermissionCreateRequest(
        @NotBlank String roleCode,
        @NotBlank String menuKey,
        boolean canList,
        boolean canCreate,
        boolean canEdit,
        boolean canDelete
) {
}
