package com.example.samplerabbitmqintegration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.samplerabbitmqintegration.settlement.api.DispatchSettlementRequest;
import com.example.samplerabbitmqintegration.settlement.application.SettlementDispatchService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class SampleRabbitmqIntegrationApplicationTests {

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management");

    @DynamicPropertySource
    static void registerRabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
    }

    @Autowired
    private SettlementDispatchService dispatchService;

    @Test
    void dispatchedSettlementIsEventuallyProcessed() {
        var response = dispatchService.dispatch(
                new DispatchSettlementRequest("SET-1001", "FX-BOOK", "USD", 100_000, "BANK-A")
        );

        Awaitility.await()
                .untilAsserted(() -> assertThat(dispatchService.getStatus(response.settlementId()).status())
                        .isEqualTo("PROCESSED"));
    }
}
