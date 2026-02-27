package com.quant.portal.api.infrastructure.audit;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentAuditorAware implements AuditorAware<String> {

    private static final String SYSTEM = "system";
    private static final String ANONYMOUS = "anonymous";

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            if (isSystemThread()) {
                return Optional.of(SYSTEM);
            }
            return Optional.of(ANONYMOUS);
        }

        String principal = authentication.getName();
        if (principal == null || principal.isBlank()) {
            return Optional.of(ANONYMOUS);
        }

        return Optional.of(principal);
    }

    private boolean isSystemThread() {
        String threadName = Thread.currentThread().getName();
        return threadName.startsWith("scheduling-")
                || threadName.startsWith("task-")
                || threadName.startsWith("batch-");
    }
}
