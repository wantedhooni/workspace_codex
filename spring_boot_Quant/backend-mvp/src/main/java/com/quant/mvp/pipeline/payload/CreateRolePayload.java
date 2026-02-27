package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public final class CreateRolePayload {

    private CreateRolePayload() {
    }

    public record Req(
            @NotBlank String roleCode,
            @NotBlank String roleName,
            String description,
            Boolean systemRole
    ) {
    }

    public record Res(
            Long roleId,
            String roleCode,
            String roleName,
            String description,
            boolean systemRole,
            Instant createdAt
    ) {
    }
}
