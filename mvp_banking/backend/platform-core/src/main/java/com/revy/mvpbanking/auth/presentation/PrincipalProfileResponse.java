package com.revy.mvpbanking.auth.presentation;

import com.revy.mvpbanking.auth.domain.PrincipalType;
import java.util.List;
import java.util.UUID;

public record PrincipalProfileResponse(
        UUID id,
        String email,
        String displayName,
        PrincipalType principalType,
        List<String> roles
) {
}
