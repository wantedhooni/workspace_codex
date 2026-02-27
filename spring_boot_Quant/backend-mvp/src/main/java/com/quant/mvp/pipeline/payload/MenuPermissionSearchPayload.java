package com.quant.mvp.pipeline.payload;

import java.time.Instant;
import java.util.List;

public final class MenuPermissionSearchPayload {

    private MenuPermissionSearchPayload() {
    }

    public record Req(
            Long roleId,
            Long menuId,
            String roleCode,
            String menuKey
    ) {
    }

    public record Item(
            Long menuPermissionId,
            Long menuId,
            String menuKey,
            Long roleId,
            String roleCode,
            boolean canRead,
            boolean canCreate,
            boolean canUpdate,
            boolean canDelete,
            Instant updatedAt
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
