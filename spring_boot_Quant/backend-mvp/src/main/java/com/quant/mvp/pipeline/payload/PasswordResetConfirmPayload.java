package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public final class PasswordResetConfirmPayload {

    private PasswordResetConfirmPayload() {
    }

    public record Req(
            @NotBlank String token,
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
