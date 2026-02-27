package com.tradingmacro.auth;

public record AuthResponse(String token, String displayName, String role) {}
