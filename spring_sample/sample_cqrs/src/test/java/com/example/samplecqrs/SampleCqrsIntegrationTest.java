package com.example.samplecqrs;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.samplecqrs.command.api.CreateOrderRequest;
import com.example.samplecqrs.command.api.OrderCommandResponse;
import com.example.samplecqrs.command.application.OrderCommandService;
import com.example.samplecqrs.command.domain.OrderStatus;
import com.example.samplecqrs.query.application.OrderSummaryQueryService;
import java.math.BigDecimal;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = "spring.profiles.active=test")
@Testcontainers
class SampleCqrsIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("sample_cqrs_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private OrderSummaryQueryService orderSummaryQueryService;

    @Test
    void 주문_명령이_조회_프로젝션에_반영된다() {
        OrderCommandResponse order = orderCommandService.createOrder(
                new CreateOrderRequest("CUST-11", "LAPTOP-15", 1, new BigDecimal("1850000"))
        );

        Awaitility.await()
                .atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> {
                    var summary = orderSummaryQueryService.getById(order.orderId());
                    assertThat(summary.customerId()).isEqualTo("CUST-11");
                    assertThat(summary.status()).isEqualTo(OrderStatus.CREATED);
                });

        orderCommandService.cancelOrder(order.orderId());

        Awaitility.await()
                .atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> {
                    var summary = orderSummaryQueryService.getById(order.orderId());
                    assertThat(summary.status()).isEqualTo(OrderStatus.CANCELLED);
                });
    }
}
