package com.derivops.mvp.auth;

public record LoginResponse(
        String accessToken,
        long expiresIn,
        String role
) {
}
