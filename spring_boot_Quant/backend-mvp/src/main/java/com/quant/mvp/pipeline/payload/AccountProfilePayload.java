package com.quant.mvp.pipeline.payload;

import com.quant.mvp.pipeline.domain.UserStatus;
import java.time.Instant;
import java.util.List;

public final class AccountProfilePayload {

    private AccountProfilePayload() {
    }

    public record Res(
            Long userId,
            String email,
            String name,
            UserStatus status,
            List<String> roleCodes,
            Instant lastLoginAt,
            Instant updatedAt
    ) {
    }
}
