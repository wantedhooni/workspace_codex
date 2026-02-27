package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.List;

public final class AssignUserRolesPayload {

    private AssignUserRolesPayload() {
    }

    public record Req(
            @NotEmpty List<@NotBlank String> roleCodes
    ) {
    }

    public record Res(
            Long userId,
            List<String> roleCodes,
            Instant updatedAt
    ) {
    }
}
