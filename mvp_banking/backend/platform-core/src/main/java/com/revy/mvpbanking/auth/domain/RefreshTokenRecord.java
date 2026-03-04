package com.revy.mvpbanking.auth.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RefreshTokenRecord(
        String tokenId,
        UUID principalId,
        String email,
        String displayName,
        PrincipalType principalType,
        List<String> roles,
        Instant expiresAt
) {
}
