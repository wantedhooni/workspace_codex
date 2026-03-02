package com.derivops.mvp.auth.dto;
import com.derivops.mvp.auth.api.*;
import com.derivops.mvp.auth.application.*;


public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        long refreshExpiresIn,
        String role
) {
}
