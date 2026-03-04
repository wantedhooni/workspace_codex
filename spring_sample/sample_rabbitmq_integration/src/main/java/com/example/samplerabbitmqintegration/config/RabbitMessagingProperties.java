package com.example.samplerabbitmqintegration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq")
public record RabbitMessagingProperties(
        String exchange,
        String queue,
        String dlx,
        String dlq,
        String routingKey,
        String dlqRoutingKey
) {
}
