package com.commerce.service_auth.auth;

import com.commerce.service_auth.user.Role;
import com.commerce.service_auth.user.User;
import com.commerce.service_auth.user.UserRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository users;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder;

    public AuthController(UserRepository users, JwtService jwtService) {
        this.users = users;
        this.jwtService = jwtService;
        this.encoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        User user = users.findByUsername(request.username()).orElseThrow();
        if (!encoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException();
        }

        List<String> scopes = defaultScopes(user.getRole());
        String token = jwtService.issueToken(
            String.valueOf(user.getId()),
            user.getTenantId(),
            List.of(user.getRole().name()),
            scopes
        );

        return Map.of(
            "token", token,
            "user", Map.of("id", user.getId(), "username", user.getUsername(), "role", user.getRole(), "tenantId", user.getTenantId())
        );
    }

    @GetMapping("/me")
    public Map<String, Object> me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return Map.of("token", authHeader);
    }

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("status", "ok");
    }

    private List<String> defaultScopes(Role role) {
        return switch (role) {
            case ADMIN -> List.of(
                "catalog:read", "catalog:write",
                "order:read", "order:write",
                "customer:read", "customer:write",
                "notification:read"
            );
            case OPERATOR -> List.of("catalog:read", "order:read", "order:write", "customer:read", "notification:read");
            case CUSTOMER -> List.of("catalog:read", "order:read", "order:write", "customer:read", "customer:write");
        };
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    private static class UnauthorizedException extends RuntimeException {}
}
