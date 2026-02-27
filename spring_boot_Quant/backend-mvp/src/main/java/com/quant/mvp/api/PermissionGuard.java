package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.service.AccessControlService;
import java.util.Locale;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class PermissionGuard {

    public static final String USER_HEADER = "X-DEMO-USER-EMAIL";

    private final AccessControlService accessControlService;

    public PermissionGuard(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    public void require(String userEmail, String menuKey, PermissionAction action) {
        String resolvedEmail = resolveUserEmail(userEmail);
        if (accessControlService.hasMenuPermission(resolvedEmail, menuKey, action)) {
            return;
        }
        throw new PermissionDeniedException(menuKey, action);
    }

    public String resolveUserEmail(String requestedUserEmail) {
        String authenticated = authenticatedUserEmail();
        if (requestedUserEmail == null || requestedUserEmail.isBlank()) {
            return authenticated;
        }

        String normalizedRequested = requestedUserEmail.trim().toLowerCase(Locale.ROOT);
        if (!normalizedRequested.equalsIgnoreCase(authenticated)) {
            throw new IllegalArgumentException("requested userEmail does not match authenticated token subject");
        }
        return authenticated;
    }

    private String authenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("authenticated user context not found");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof String email && !email.isBlank() && !"anonymousUser".equals(email)) {
            return email.trim().toLowerCase(Locale.ROOT);
        }

        throw new IllegalArgumentException("authenticated principal email is missing");
    }
}
