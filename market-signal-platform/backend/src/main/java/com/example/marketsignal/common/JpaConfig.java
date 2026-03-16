package com.example.marketsignal.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 감사 기능을 활성화한다.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
