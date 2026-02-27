package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public final class ChangePasswordPayload {

    private ChangePasswordPayload() {
    }

    public record Req(
            @NotBlank String currentPassword,
            @NotBlank String newPassword
    ) {
    }

    public record Res(
            Long userId,
            boolean changed,
            Instant changedAt
    ) {
    }
}
