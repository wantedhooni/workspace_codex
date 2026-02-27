package com.quant.mvp.pipeline.payload;

import java.time.Instant;

public class DeleteOrderPayload {

    public record Res(
            Long orderId,
            String status,
            Instant deletedAt
    ) {
    }
}
