package com.quant.portal.api.infrastructure.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class CurrentAuditorAwareTest {

    private final CurrentAuditorAware auditorAware = new CurrentAuditorAware();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnAuthenticatedPrincipal() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "portfolio-user",
                "secret",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(auditorAware.getCurrentAuditor()).contains("portfolio-user");
    }

    @Test
    void shouldReturnAnonymousWhenAuthenticationIsMissing() {
        SecurityContextHolder.clearContext();

        assertThat(auditorAware.getCurrentAuditor()).contains("anonymous");
    }

    @Test
    void shouldReturnSystemForBatchThreadWhenAuthenticationIsMissing() throws InterruptedException {
        AtomicReference<String> result = new AtomicReference<>();
        Thread thread = new Thread(
                () -> result.set(auditorAware.getCurrentAuditor().orElse(null)),
                "batch-worker-1"
        );

        thread.start();
        thread.join();

        assertThat(result.get()).isEqualTo("system");
    }
}
