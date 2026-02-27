package com.portal.admin.service;

import com.portal.admin.auth.JwtService;
import com.portal.admin.domain.AdminUser;
import com.portal.admin.repo.AdminUserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Objects;

@Service
public class AuthService {

    private final AdminUserRepository users;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AdminUserRepository users, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResult login(String username, String password) {
        var user = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials"));
        if (!matchesPassword(user, password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
        }
        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getTokenVersion());
        return new LoginResult(token, toUserPayload(user.getId(), user.getUsername()));
    }

    public Map<String, Object> me(String token) {
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        Claims claims = parseClaims(token);
        String username = claims.getSubject();
        Integer tokenVersion = parseTokenVersion(claims);
        var user = users.findByUsername(username).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if (!Objects.equals(tokenVersion, user.getTokenVersion())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "stale token");
        }
        return toUserPayload(user.getId(), user.getUsername());
    }

    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        Claims claims = parseClaims(token);
        Long userId = parseUserId(claims);
        AdminUser user = userId == null ? null : users.findById(userId).orElse(null);
        if (user == null) {
            return;
        }
        user.incrementTokenVersion();
        users.save(user);
    }

    private boolean matchesPassword(AdminUser user, String rawPassword) {
        String storedHash = user.getPasswordHash();
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }
        try {
            if (passwordEncoder.matches(rawPassword, storedHash)) {
                return true;
            }
        } catch (IllegalArgumentException ignored) {
            // legacy plaintext or invalid format
        }

        // one-time migration for existing demo/plain credentials
        if (storedHash.equals(rawPassword)) {
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            user.incrementTokenVersion();
            users.save(user);
            return true;
        }
        return false;
    }

    private Claims parseClaims(String token) {
        try {
            return jwtService.parseToken(token);
        } catch (Exception ignored) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token");
        }
    }

    private Integer parseTokenVersion(Claims claims) {
        Object value = claims.get("tokenVersion");
        if (value instanceof Integer integerValue) {
            return integerValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token");
    }

    private Long parseUserId(Claims claims) {
        Object value = claims.get("userId");
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Integer intValue) {
            return intValue.longValue();
        }
        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }
        return null;
    }

    private Map<String, Object> toUserPayload(Long id, String username) {
        return Map.of("id", id, "username", username);
    }

    public record LoginResult(String token, Map<String, Object> user) {
    }
}
