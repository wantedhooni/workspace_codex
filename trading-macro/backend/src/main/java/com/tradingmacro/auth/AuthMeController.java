package com.tradingmacro.auth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AuthMeController {
    @GetMapping("/api/auth/me")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of(
            "name", authentication.getName(),
            "authorities", authentication.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toList())
        );
    }

    @GetMapping("/api/auth/test-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> testAdmin() {
        return Map.of("status", "ok");
    }

    @GetMapping("/api/auth/test-any-role")
    @PreAuthorize("hasAnyRole('ADMIN','TRADER')")
    public Map<String, String> testAnyRole() {
        return Map.of("status", "ok");
    }
}
