package com.example.securities.auth;

import java.util.List;

public class AuthDtos {

    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(String token, String username, List<String> roles, long expiresInSeconds) {
    }
}
