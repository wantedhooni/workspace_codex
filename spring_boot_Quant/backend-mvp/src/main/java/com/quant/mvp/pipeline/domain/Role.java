package com.quant.mvp.pipeline.domain;

import java.time.Instant;

public record Role(
        Long roleId,
        String roleCode,
        String roleName,
        String description,
        boolean systemRole,
        Instant createdAt
) {
}
