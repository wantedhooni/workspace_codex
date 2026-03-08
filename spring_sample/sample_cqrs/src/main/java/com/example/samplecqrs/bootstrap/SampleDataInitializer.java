package com.example.samplecqrs.bootstrap;

import com.example.samplecqrs.command.api.CreateOrderRequest;
import com.example.samplecqrs.command.api.OrderCommandResponse;
import com.example.samplecqrs.command.application.OrderCommandService;
import com.example.samplecqrs.command.domain.PurchaseOrderRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class SampleDataInitializer {

    @Bean
    CommandLineRunner sampleCqrsDataLoader(
            PurchaseOrderRepository purchaseOrderRepository,
            OrderCommandService orderCommandService
    ) {
        return args -> {
            if (purchaseOrderRepository.count() > 0) {
                return;
            }

            orderCommandService.createOrder(new CreateOrderRequest("CUST-100", "MONITOR-32", 2, new BigDecimal("320000")));
            orderCommandService.createOrder(new CreateOrderRequest("CUST-101", "KEYBOARD-MECH", 5, new BigDecimal("119000")));
            OrderCommandResponse cancelled = orderCommandService.createOrder(
                    new CreateOrderRequest("CUST-102", "DOCK-USB-C", 1, new BigDecimal("159000"))
            );
            orderCommandService.cancelOrder(cancelled.orderId());
        };
    }
}
