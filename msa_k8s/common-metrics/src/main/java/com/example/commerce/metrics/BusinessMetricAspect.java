package com.example.commerce.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * {@link BusinessMetric}이 선언된 서비스 메서드의 처리시간과 결과를 Micrometer Timer로 기록한다.
 */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class BusinessMetricAspect {

    /** 모든 업무 서비스가 공유하는 Micrometer Timer 이름이다. */
    public static final String METRIC_NAME = "commerce.business.operation.duration";

    private final MeterRegistry meterRegistry;

    /**
     * 업무 메트릭을 등록할 MeterRegistry를 주입받는다.
     *
     * @param meterRegistry 공통 메트릭 저장소
     */
    public BusinessMetricAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 대상 서비스 메서드를 실행하고 성공 또는 실패 결과와 소요시간을 기록한다.
     *
     * @param joinPoint 대상 메서드 호출
     * @param businessMetric 업무 메트릭 선언
     * @return 대상 메서드 실행 결과
     * @throws Throwable 대상 메서드가 발생시킨 예외
     */
    @Around("@annotation(businessMetric)")
    public Object record(
            ProceedingJoinPoint joinPoint,
            BusinessMetric businessMetric) throws Throwable {
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "success";
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            outcome = "failure";
            throw throwable;
        } finally {
            Timer timer = Timer.builder(METRIC_NAME)
                    .description("업무 서비스 작업 처리시간과 결과")
                    .tag("operation", businessMetric.value())
                    .tag("outcome", outcome)
                    .publishPercentileHistogram()
                    .register(meterRegistry);
            sample.stop(timer);
        }
    }
}
