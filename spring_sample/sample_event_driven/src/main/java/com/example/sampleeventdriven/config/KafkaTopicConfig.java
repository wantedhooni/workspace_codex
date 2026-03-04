package com.example.sampleeventdriven.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableConfigurationProperties(EventFlowProperties.class)
public class KafkaTopicConfig {

    @Bean
    @ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
    public NewTopic ordersTopic(EventFlowProperties properties) {
        return TopicBuilder.name(properties.getOrdersTopic()).partitions(1).replicas(1).build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
    public NewTopic paymentsTopic(EventFlowProperties properties) {
        return TopicBuilder.name(properties.getPaymentsTopic()).partitions(1).replicas(1).build();
    }
}
