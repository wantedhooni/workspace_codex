package com.quant.mvp.pipeline.domain;

import java.time.Instant;

public record AccountSession(
        Long sessionId,
        Long userId,
        String ipAddress,
        String userAgent,
        boolean active,
        Instant createdAt,
        Instant lastAccessAt
) {

    public AccountSession withActive(boolean newActive) {
        return new AccountSession(
                sessionId,
                userId,
                ipAddress,
                userAgent,
                newActive,
                createdAt,
                Instant.now()
        );
    }
}
