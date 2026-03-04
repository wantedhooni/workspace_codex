package com.example.samplekafkaintegration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.samplekafkaintegration.messaging.api.TradeInstructionRequest;
import com.example.samplekafkaintegration.messaging.application.TradeInstructionService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"trade.instructions.v1", "trade.instructions.v1.dlt"})
class SampleKafkaIntegrationApplicationTests {

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> System.getProperty("spring.embedded.kafka.brokers"));
    }

    @Autowired
    private TradeInstructionService tradeInstructionService;

    @Test
    void publishedInstructionIsEventuallyProcessed() {
        var response = tradeInstructionService.publish(
                new TradeInstructionRequest("ACC-1001", "EURUSD-FWD", 10_000, "BANK-A")
        );

        Awaitility.await()
                .untilAsserted(() -> assertThat(tradeInstructionService.getStatus(response.messageId()).status())
                        .isEqualTo("PROCESSED"));
    }
}
