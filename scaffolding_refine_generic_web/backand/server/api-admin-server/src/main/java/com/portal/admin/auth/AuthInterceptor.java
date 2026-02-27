package com.portal.admin.auth;

import com.portal.admin.domain.AdminUser;
import com.portal.admin.repo.AdminUserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String COOKIE_NAME = "ADMIN_TOKEN";

    private final JwtService jwtService;
    private final AdminUserRepository users;

    public AuthInterceptor(JwtService jwtService, AdminUserRepository users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            return true;
        }
        if (path.startsWith("/auth")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")) {
            return true;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        String token = Arrays.stream(cookies)
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (token == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        try {
            Claims claims = jwtService.parseToken(token);
            Long userId = parseUserId(claims);
            Integer tokenVersion = parseTokenVersion(claims);
            AdminUser user = userId == null ? null : users.findById(userId).orElse(null);
            if (user == null || tokenVersion == null) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return false;
            }
            if (!claims.getSubject().equals(user.getUsername()) || !tokenVersion.equals(user.getTokenVersion())) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return false;
            }
            request.setAttribute("auth.username", claims.getSubject());
            request.setAttribute("auth.userId", userId);
            request.setAttribute("auth.tokenVersion", tokenVersion);
            return true;
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
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

    private Integer parseTokenVersion(Claims claims) {
        Object value = claims.get("tokenVersion");
        if (value instanceof Integer integerValue) {
            return integerValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }
        return null;
    }
}
