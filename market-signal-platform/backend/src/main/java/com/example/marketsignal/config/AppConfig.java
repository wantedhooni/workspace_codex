package com.example.marketsignal.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 커스텀 설정 프로퍼티를 활성화한다.
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {
}
