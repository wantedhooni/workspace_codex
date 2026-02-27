package com.quant.mvp.pipeline.payload;

import com.quant.mvp.pipeline.domain.UserStatus;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public final class UpdateUserStatusPayload {

    private UpdateUserStatusPayload() {
    }

    public record Req(
            @NotNull UserStatus status
    ) {
    }

    public record Res(
            Long userId,
            UserStatus status,
            Instant updatedAt
    ) {
    }
}
