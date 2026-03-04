package com.revy.mvpbanking.auth.domain;

import java.util.List;
import java.util.UUID;

public record AuthenticatedPrincipal(
        UUID id,
        String email,
        String displayName,
        PrincipalType principalType,
        List<String> roles,
        boolean active
) {
}
