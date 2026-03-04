package com.revy.mvpbanking.auth.domain;

public record AuthenticationResult(
        AuthenticatedPrincipal principal,
        TokenPair tokens
) {
}
