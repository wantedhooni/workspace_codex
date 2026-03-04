package com.revy.mvpbanking.auth.presentation;

import com.revy.mvpbanking.auth.domain.AuthenticationResult;
import com.revy.mvpbanking.auth.domain.PrincipalType;
import com.revy.mvpbanking.auth.domain.TokenPair;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        PrincipalType principalType,
        String email,
        String displayName
) {
    public static AuthResponse of(TokenPair tokenPair, PrincipalType principalType, String email, String displayName) {
        return new AuthResponse(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.accessTokenExpiresIn(),
                tokenPair.refreshTokenExpiresIn(),
                principalType,
                email,
                displayName
        );
    }

    public static AuthResponse of(AuthenticationResult result) {
        return of(
                result.tokens(),
                result.principal().principalType(),
                result.principal().email(),
                result.principal().displayName()
        );
    }
}
