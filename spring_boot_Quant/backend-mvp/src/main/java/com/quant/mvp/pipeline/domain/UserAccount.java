package com.quant.mvp.pipeline.domain;

import java.time.Instant;

public record UserAccount(
        Long userId,
        String email,
        String name,
        UserStatus status,
        Instant lastLoginAt,
        Instant createdAt,
        Instant updatedAt
) {

    public UserAccount withStatus(UserStatus newStatus) {
        return new UserAccount(
                userId,
                email,
                name,
                newStatus,
                lastLoginAt,
                createdAt,
                Instant.now()
        );
    }

    public UserAccount withLastLogin(Instant newLastLoginAt) {
        return new UserAccount(
                userId,
                email,
                name,
                status,
                newLastLoginAt,
                createdAt,
                Instant.now()
        );
    }
}
