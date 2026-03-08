package com.example.samplesaga.bootstrap;

import com.example.samplesaga.order.api.CreateSagaOrderRequest;
import com.example.samplesaga.order.domain.SagaOrderRepository;
import com.example.samplesaga.saga.application.OrderSagaOrchestrator;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Saga 성공/실패/보상 시나리오를 확인할 수 있도록 샘플 주문을 적재한다.
 */
@Configuration
@Profile("!test")
public class SampleDataInitializer {

    /**
     * 샘플 Saga 실행 데이터를 초기 적재하는 러너를 만든다.
     *
     * @param sagaOrderRepository 주문 존재 여부 확인용 저장소
     * @param orderSagaOrchestrator Saga 시작 오케스트레이터
     * @return 애플리케이션 시작 후 실행되는 러너
     */
    @Bean
    CommandLineRunner sampleSagaDataLoader(
            SagaOrderRepository sagaOrderRepository,
            OrderSagaOrchestrator orderSagaOrchestrator
    ) {
        return args -> {
            if (sagaOrderRepository.count() > 0) {
                return;
            }

            orderSagaOrchestrator.start(new CreateSagaOrderRequest("CUST-700", "DESK-01", 1, new BigDecimal("210000")));
            orderSagaOrchestrator.start(new CreateSagaOrderRequest("FAIL-PAYMENT", "DESK-02", 1, new BigDecimal("150000")));
            orderSagaOrchestrator.start(new CreateSagaOrderRequest("CUST-701", "LIMITED-STOCK", 2, new BigDecimal("99000")));
        };
    }
}
