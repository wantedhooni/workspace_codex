package com.commerce.service_catalog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AccessPolicyFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        String rolesHeader = request.getHeader("X-Roles");
        String scopesHeader = request.getHeader("X-Scopes");

        Set<String> roles = split(rolesHeader);
        if (roles.contains("ADMIN")) {
            filterChain.doFilter(request, response);
            return;
        }

        Set<String> scopes = split(scopesHeader);
        String needed = requiredScope(request.getMethod(), path);
        if (needed != null && scopes.contains(needed)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.FORBIDDEN.value());
    }

    private String requiredScope(String method, String path) {
        if (!path.startsWith("/catalog")) {
            return null;
        }
        if ("GET".equalsIgnoreCase(method)) {
            return "catalog:read";
        }
        return "catalog:write";
    }

    private Set<String> split(String raw) {
        if (raw == null || raw.isBlank()) return Set.of();
        return Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
    }
}
