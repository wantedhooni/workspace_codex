package com.quant.portal.api.config;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "currentAuditorAware")
public class JpaAuditConfig {
}
