package com.derivops.mvp.config;

import com.derivops.mvp.common.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SecurityExceptionHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final org.slf4j.Logger SECURITY_AUDIT = org.slf4j.LoggerFactory.getLogger("SECURITY_AUDIT");

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        SECURITY_AUDIT.warn("auth failed path={} reason={}", request.getRequestURI(), authException.getMessage());
        writeError(response, request.getRequestURI(), HttpStatus.UNAUTHORIZED, "Authentication required");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        SECURITY_AUDIT.warn("access denied path={} reason={}", request.getRequestURI(), accessDeniedException.getMessage());
        writeError(response, request.getRequestURI(), HttpStatus.FORBIDDEN, "Access denied");
    }

    private void writeError(HttpServletResponse response, String path, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError body = new ApiError(OffsetDateTime.now(), status.value(), status.getReasonPhrase(), message, path);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
