package com.quant.mvp.pipeline.payload;

import java.time.Instant;
import java.util.List;

public final class RoleSearchPayload {

    private RoleSearchPayload() {
    }

    public record Req(
            Long roleId,
            String roleCode,
            String roleName
    ) {
    }

    public record Item(
            Long roleId,
            String roleCode,
            String roleName,
            String description,
            boolean systemRole,
            Instant createdAt
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
