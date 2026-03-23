package com.example.samplegoogleoauth.auth.dto;

public record TokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresInSeconds
) {
}
