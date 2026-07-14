package com.example.commerce.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * 공통 태그와 업무 메트릭 성공·실패 기록을 검증한다.
 */
class CommonMetricsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommonMetricsAutoConfiguration.class))
            .withBean(SimpleMeterRegistry.class)
            .withPropertyValues(
                    "spring.application.name=metrics-test-service",
                    "commerce.observability.metrics.environment=test");

    /**
     * 자동 구성에서 애플리케이션과 환경 공통 태그를 MeterRegistry에 적용하는지 검증한다.
     */
    @Test
    void configuresCommonTags() {
        contextRunner.run(context -> {
            SimpleMeterRegistry registry = context.getBean(SimpleMeterRegistry.class);
            MeterRegistryCustomizer<?> customizer =
                    context.getBean(MeterRegistryCustomizer.class);
            @SuppressWarnings("unchecked")
            MeterRegistryCustomizer<SimpleMeterRegistry> typedCustomizer =
                    (MeterRegistryCustomizer<SimpleMeterRegistry>) customizer;
            typedCustomizer.customize(registry);

            Counter counter = registry.counter("common.metrics.test");

            assertThat(counter.getId().getTag("application")).isEqualTo("metrics-test-service");
            assertThat(counter.getId().getTag("environment")).isEqualTo("test");
            assertThat(context).hasSingleBean(BusinessMetricAspect.class);
        });
    }

    /**
     * 업무 메트릭 Aspect가 성공과 실패 호출을 별도 outcome으로 기록하는지 검증한다.
     */
    @Test
    void recordsBusinessOperationOutcome() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(new MetricTarget());
        proxyFactory.addAspect(new BusinessMetricAspect(registry));
        MetricTarget proxy = proxyFactory.getProxy();

        assertThat(proxy.success()).isEqualTo("ok");
        assertThatThrownBy(proxy::failure).isInstanceOf(IllegalStateException.class);

        assertThat(registry.get(BusinessMetricAspect.METRIC_NAME)
                .tag("operation", "test.success")
                .tag("outcome", "success")
                .timer()
                .count()).isEqualTo(1);
        assertThat(registry.get(BusinessMetricAspect.METRIC_NAME)
                .tag("operation", "test.failure")
                .tag("outcome", "failure")
                .timer()
                .count()).isEqualTo(1);
    }

    /**
     * Aspect 단위 테스트에서 사용할 업무 메트릭 대상 서비스다.
     */
    static class MetricTarget {

        /**
         * 정상 처리되는 테스트 작업이다.
         *
         * @return 고정 성공값
         */
        @BusinessMetric("test.success")
        public String success() {
            return "ok";
        }

        /**
         * 실패 결과 기록을 확인하기 위해 예외를 발생시킨다.
         */
        @BusinessMetric("test.failure")
        public void failure() {
            throw new IllegalStateException("expected");
        }
    }
}
