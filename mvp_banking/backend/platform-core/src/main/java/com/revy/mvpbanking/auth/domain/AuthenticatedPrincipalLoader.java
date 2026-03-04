package com.revy.mvpbanking.auth.domain;

import java.util.Optional;
import java.util.UUID;

public interface AuthenticatedPrincipalLoader {
    Optional<AuthenticatedPrincipal> load(PrincipalType principalType, UUID principalId);
}
