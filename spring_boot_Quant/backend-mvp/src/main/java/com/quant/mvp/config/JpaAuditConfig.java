package com.quant.mvp.config;

import java.util.Locale;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class JpaAuditConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }

            Object principal = authentication.getPrincipal();
            if (principal instanceof String email) {
                String normalized = email.trim().toLowerCase(Locale.ROOT);
                if (!normalized.isBlank() && !"anonymoususer".equals(normalized)) {
                    return Optional.of(normalized);
                }
            }

            return Optional.of("anonymous");
        };
    }
}
