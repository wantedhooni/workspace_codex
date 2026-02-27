package com.quant.mvp.pipeline.payload;

import java.time.Instant;

public class DeleteMenuPermissionPayload {

    public record Res(
            Long menuPermissionId,
            Long menuId,
            Long roleId,
            Instant deletedAt
    ) {
    }
}
