package com.quant.mvp.pipeline.payload;

import java.time.Instant;

public class DeleteUserPayload {

    public record Res(
            Long userId,
            String email,
            Instant deletedAt
    ) {
    }
}
