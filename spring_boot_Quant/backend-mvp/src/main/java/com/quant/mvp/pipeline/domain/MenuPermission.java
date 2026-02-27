package com.quant.mvp.pipeline.domain;

import java.time.Instant;

public record MenuPermission(
        Long menuPermissionId,
        Long menuId,
        Long roleId,
        boolean canRead,
        boolean canCreate,
        boolean canUpdate,
        boolean canDelete,
        Instant updatedAt
) {
}
