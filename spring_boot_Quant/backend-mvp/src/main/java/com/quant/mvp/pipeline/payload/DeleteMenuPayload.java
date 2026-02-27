package com.quant.mvp.pipeline.payload;

import java.time.Instant;

public class DeleteMenuPayload {

    public record Res(
            Long menuId,
            String menuKey,
            Instant deletedAt
    ) {
    }
}
