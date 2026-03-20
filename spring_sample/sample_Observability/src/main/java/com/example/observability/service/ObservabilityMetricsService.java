package com.example.observability.service;

import com.example.observability.domain.CustomerOrder;
import com.example.observability.domain.OrderStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import org.springframework.stereotype.Service;

/**
 * 주문 운영 이벤트를 Prometheus용 비즈니스 메트릭으로 기록한다.
 */
@Service
public class ObservabilityMetricsService {

    private final MeterRegistry meterRegistry;

    public ObservabilityMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 신규 주문 등록 이벤트와 예상 처리 시간을 기록한다.
     */
    public void recordOrderCreated(CustomerOrder order, Duration expectedLeadTime) {
        Counter.builder("sample_order_created_total")
                .description("테넌트와 우선순위 기준 신규 주문 생성 건수")
                .tag("tenant", order.getTenantId())
                .tag("priority", order.getPriority())
                .register(meterRegistry)
                .increment();

        Timer.builder("sample_order_expected_lead_time")
                .description("주문 생성 시점의 예상 처리 소요 시간")
                .tag("tenant", order.getTenantId())
                .tag("priority", order.getPriority())
                .register(meterRegistry)
                .record(expectedLeadTime);
    }

    /**
     * 상태 변경 이벤트와 처리 시간 메트릭을 함께 기록한다.
     */
    public void recordOrderStatusChanged(CustomerOrder order, OrderStatus previousStatus) {
        Counter.builder("sample_order_status_changed_total")
                .description("주문 상태 변경 건수")
                .tag("tenant", order.getTenantId())
                .tag("from", previousStatus.name())
                .tag("to", order.getStatus().name())
                .register(meterRegistry)
                .increment();

        Duration processingDuration = Duration.between(order.getCreatedAt(), order.getUpdatedAt());
        Timer.builder("sample_order_processing_time")
                .description("주문 생성 이후 현재 상태까지의 경과 시간")
                .tag("tenant", order.getTenantId())
                .tag("status", order.getStatus().name())
                .register(meterRegistry)
                .record(processingDuration);
    }
}
