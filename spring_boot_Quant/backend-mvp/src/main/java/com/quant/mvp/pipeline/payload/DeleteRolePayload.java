package com.quant.mvp.pipeline.payload;

import java.time.Instant;

public class DeleteRolePayload {

    public record Res(
            Long roleId,
            String roleCode,
            Instant deletedAt
    ) {
    }
}
