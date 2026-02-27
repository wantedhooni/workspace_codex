package com.derivops.mvp.common;

import com.derivops.mvp.user.UserRole;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "system" : auth.getName();
    }

    public static Optional<UserRole> currentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().isEmpty()) {
            return Optional.empty();
        }
        String authority = auth.getAuthorities().iterator().next().getAuthority();
        if (!authority.startsWith("ROLE_")) {
            return Optional.empty();
        }
        return Optional.of(UserRole.valueOf(authority.substring("ROLE_".length())));
    }
}
