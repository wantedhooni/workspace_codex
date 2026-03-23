package com.example.samplegoogleoauth.auth.dto;

public record UserProfileResponse(
    boolean authenticated,
    boolean registered,
    String name,
    String email,
    String picture,
    String provider,
    String displayName,
    String organization,
    String jobTitle,
    boolean marketingConsent
) {
}
