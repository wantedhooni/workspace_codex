package com.quant.mvp.pipeline.payload;

import com.quant.mvp.pipeline.domain.UserStatus;
import java.time.Instant;
import java.util.List;

public final class UserSearchPayload {

    private UserSearchPayload() {
    }

    public record Req(
            Long userId,
            String email,
            String name,
            String status,
            String roleCode
    ) {
    }

    public record Item(
            Long userId,
            String email,
            String name,
            UserStatus status,
            List<String> roleCodes,
            Instant lastLoginAt,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
