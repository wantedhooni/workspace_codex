package com.example.commerce.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * 서비스명과 배포 환경 태그, 카디널리티 제한, 업무 메트릭 Aspect를 자동 구성한다.
 */
@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnProperty(
        prefix = "commerce.observability.metrics",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(CommonMetricsProperties.class)
public class CommonMetricsAutoConfiguration {

    /**
     * 공통 메트릭 자동 구성 클래스를 생성한다.
     */
    public CommonMetricsAutoConfiguration() {
    }

    /**
     * 애플리케이션과 환경 공통 태그를 추가하고 동적 태그 개수를 제한한다.
     *
     * @param environment Spring 환경 정보
     * @param properties 공통 메트릭 설정
     * @return 모든 MeterRegistry에 적용할 사용자 정의 설정
     */
    @Bean
    MeterRegistryCustomizer<MeterRegistry> commonMeterRegistryCustomizer(
            Environment environment,
            CommonMetricsProperties properties) {
        String application = environment.getProperty("spring.application.name", "unknown");
        return registry -> registry.config()
                .commonTags(
                        "application", application,
                        "environment", properties.getEnvironment())
                .meterFilter(MeterFilter.maximumAllowableTags(
                        "http.server.requests",
                        "uri",
                        properties.getMaxUriTags(),
                        MeterFilter.deny()))
                .meterFilter(MeterFilter.maximumAllowableTags(
                        BusinessMetricAspect.METRIC_NAME,
                        "operation",
                        properties.getMaxOperationTags(),
                        MeterFilter.deny()));
    }

    /**
     * 서비스 메서드의 공통 업무 지표를 기록하는 Aspect를 생성한다.
     *
     * @param meterRegistry 공통 메트릭 저장소
     * @return 업무 메트릭 Aspect
     */
    @Bean
    @ConditionalOnMissingBean
    BusinessMetricAspect businessMetricAspect(MeterRegistry meterRegistry) {
        return new BusinessMetricAspect(meterRegistry);
    }
}
