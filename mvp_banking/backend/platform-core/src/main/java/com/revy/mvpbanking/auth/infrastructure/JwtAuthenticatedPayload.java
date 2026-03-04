package com.revy.mvpbanking.auth.infrastructure;

import com.revy.mvpbanking.auth.domain.PrincipalType;
import java.util.List;
import java.util.UUID;

public record JwtAuthenticatedPayload(
        UUID principalId,
        String email,
        String displayName,
        PrincipalType principalType,
        List<String> roles
) {
}
