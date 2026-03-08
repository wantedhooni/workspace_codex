package com.example.transactionaloutbox;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.transactionaloutbox.order.api.CreateOrderRequest;
import com.example.transactionaloutbox.order.api.OrderResponse;
import com.example.transactionaloutbox.order.application.OrderCommandService;
import com.example.transactionaloutbox.outbox.domain.OutboxEvent;
import com.example.transactionaloutbox.outbox.domain.OutboxEventRepository;
import com.example.transactionaloutbox.outbox.domain.OutboxStatus;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "app.outbox.fixed-delay=100ms",
        "app.outbox.publish-timeout=3s",
        "app.outbox.retry-backoff=200ms"
})
@EmbeddedKafka(partitions = 1, topics = {"order.created.v1"})
class TransactionalOutboxIntegrationTest {

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    void 주문_생성시_아웃박스_이벤트가_저장되고_Kafka로_발행된다() {
        OrderResponse order = orderCommandService.createOrder(
                new CreateOrderRequest("CUST-1", "SKU-1", 3, new BigDecimal("10000"))
        );

        org.awaitility.Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    OutboxEvent event = outboxEventRepository.findAll().stream().findFirst().orElseThrow();
                    assertThat(event.getAggregateId()).isEqualTo(order.orderId());
                    assertThat(event.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
                });

        ConsumerRecord<String, String> record = consumeSingleRecord();
        assertThat(record.key()).isEqualTo(order.orderId());
        assertThat(record.value()).contains(order.orderId());
        assertThat(record.value()).contains("\"productId\":\"SKU-1\"");
    }

    private ConsumerRecord<String, String> consumeSingleRecord() {
        Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker);
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(
                consumerProperties,
                new StringDeserializer(),
                new StringDeserializer()
        ).createConsumer()) {
            embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "order.created.v1");
            return KafkaTestUtils.getSingleRecord(consumer, "order.created.v1", Duration.ofSeconds(10));
        }
    }
}
