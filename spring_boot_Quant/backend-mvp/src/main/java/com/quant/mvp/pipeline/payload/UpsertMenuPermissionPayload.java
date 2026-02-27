package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public final class UpsertMenuPermissionPayload {

    private UpsertMenuPermissionPayload() {
    }

    public record Req(
            @NotNull Long menuId,
            @NotNull Long roleId,
            @NotNull Boolean canRead,
            @NotNull Boolean canCreate,
            @NotNull Boolean canUpdate,
            @NotNull Boolean canDelete
    ) {
    }

    public record Res(
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
}
