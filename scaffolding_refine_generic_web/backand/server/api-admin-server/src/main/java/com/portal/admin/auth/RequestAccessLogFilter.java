package com.portal.admin.auth;

import com.portal.admin.domain.AccessLog;
import com.portal.admin.repo.AccessLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class RequestAccessLogFilter extends OncePerRequestFilter {

    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern TOKEN_LIKE_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{24,}$");

    private final AccessLogRepository accessLogs;

    public RequestAccessLogFilter(AccessLogRepository accessLogs) {
        this.accessLogs = accessLogs;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (shouldSkip(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            String username = maskUsername(resolveUsername(request));
            String ipAddress = maskClientIp(resolveClientIp(request));
            String action = resolveAction(request.getMethod(), path);
            String sanitizedPath = sanitizePath(path);
            boolean success = response.getStatus() < 400;
            accessLogs.save(new AccessLog(username, ipAddress, action, sanitizedPath, success));
        }
    }

    private boolean shouldSkip(String path) {
        return path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")
                || path.startsWith("/error");
    }

    private String resolveUsername(HttpServletRequest request) {
        Object authUsername = request.getAttribute("auth.username");
        if (authUsername instanceof String username && !username.isBlank()) {
            return username;
        }
        return "anonymous";
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return request.getRemoteAddr();
        }
        int comma = forwardedFor.indexOf(",");
        return comma > 0 ? forwardedFor.substring(0, comma).trim() : forwardedFor.trim();
    }

    private String resolveAction(String method, String path) {
        if ("/auth/login".equals(path)) {
            return "LOGIN";
        }
        if ("/auth/logout".equals(path)) {
            return "LOGOUT";
        }
        return method.toUpperCase();
    }

    private String maskUsername(String username) {
        if (username == null || username.isBlank()) {
            return "anonymous";
        }
        if ("anonymous".equalsIgnoreCase(username)) {
            return "anonymous";
        }
        if (username.length() <= 2) {
            return username.charAt(0) + "*";
        }
        if (username.length() <= 5) {
            return username.substring(0, 1) + "***";
        }
        return username.substring(0, 2) + "***" + username.charAt(username.length() - 1);
    }

    private String maskClientIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return "0.0.0.0";
        }
        String raw = ipAddress.trim();
        if (raw.contains(".")) {
            String[] parts = raw.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + "." + parts[2] + ".0";
            }
            return "0.0.0.0";
        }
        if (raw.contains(":")) {
            String[] parts = raw.split(":");
            if (parts.length >= 2) {
                return (parts[0].isBlank() ? "0" : parts[0]) + ":" + (parts[1].isBlank() ? "0" : parts[1]) + "::";
            }
            return "::";
        }
        return "0.0.0.0";
    }

    private String sanitizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        String normalized = path.split("\\?")[0].trim();
        if (normalized.isEmpty()) {
            return "/";
        }
        String[] rawSegments = normalized.split("/");
        List<String> sanitizedSegments = new ArrayList<>();
        for (String segment : rawSegments) {
            if (segment == null || segment.isBlank()) {
                continue;
            }
            sanitizedSegments.add(sanitizeSegment(segment));
        }
        return "/" + String.join("/", sanitizedSegments);
    }

    private String sanitizeSegment(String segment) {
        String trimmed = segment.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        if (NUMERIC_PATTERN.matcher(trimmed).matches()) {
            return ":id";
        }
        if (UUID_PATTERN.matcher(trimmed).matches()) {
            return ":uuid";
        }
        if (TOKEN_LIKE_PATTERN.matcher(trimmed).matches()) {
            return ":token";
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }
}
