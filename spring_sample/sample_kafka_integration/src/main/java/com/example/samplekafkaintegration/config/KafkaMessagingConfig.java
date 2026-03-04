package com.example.samplekafkaintegration.config;

import com.example.samplekafkaintegration.messaging.domain.TradeInstructionMessage;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
class KafkaMessagingConfig {

    @Bean
    KafkaIntegrationProperties kafkaIntegrationProperties() {
        return new KafkaIntegrationProperties("trade.instructions.v1", "trade.instructions.v1.dlt");
    }

    @Bean
    NewTopic tradeTopic(KafkaIntegrationProperties properties) {
        return new NewTopic(properties.tradeTopic(), 3, (short) 1);
    }

    @Bean
    NewTopic tradeDltTopic(KafkaIntegrationProperties properties) {
        return new NewTopic(properties.tradeDltTopic(), 3, (short) 1);
    }

    @Bean
    CommonErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate, KafkaIntegrationProperties properties) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new org.apache.kafka.common.TopicPartition(properties.tradeDltTopic(), record.partition())
        );
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1_000L, 2L));
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, TradeInstructionMessage> tradeKafkaListenerContainerFactory(
            org.springframework.kafka.core.ConsumerFactory<String, TradeInstructionMessage> consumerFactory,
            CommonErrorHandler kafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, TradeInstructionMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        return factory;
    }
}
