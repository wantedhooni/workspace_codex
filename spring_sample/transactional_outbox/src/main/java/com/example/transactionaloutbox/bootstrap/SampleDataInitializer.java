package com.example.transactionaloutbox.bootstrap;

import com.example.transactionaloutbox.order.api.CreateOrderRequest;
import com.example.transactionaloutbox.order.application.OrderCommandService;
import com.example.transactionaloutbox.order.domain.PurchaseOrderRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class SampleDataInitializer {

    @Bean
    CommandLineRunner transactionalOutboxDataLoader(
            PurchaseOrderRepository purchaseOrderRepository,
            OrderCommandService orderCommandService
    ) {
        return args -> {
            if (purchaseOrderRepository.count() > 0) {
                return;
            }

            orderCommandService.createOrder(new CreateOrderRequest("CUST-900", "SKU-OUTBOX-1", 2, new BigDecimal("129000")));
            orderCommandService.createOrder(new CreateOrderRequest("CUST-901", "SKU-OUTBOX-2", 1, new BigDecimal("249000")));
            orderCommandService.createOrder(new CreateOrderRequest("CUST-902", "SKU-OUTBOX-3", 4, new BigDecimal("45000")));
        };
    }
}
