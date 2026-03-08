package com.example.samplesaga.bootstrap;

import com.example.samplesaga.order.api.CreateSagaOrderRequest;
import com.example.samplesaga.order.domain.SagaOrderRepository;
import com.example.samplesaga.saga.application.OrderSagaOrchestrator;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class SampleDataInitializer {

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
