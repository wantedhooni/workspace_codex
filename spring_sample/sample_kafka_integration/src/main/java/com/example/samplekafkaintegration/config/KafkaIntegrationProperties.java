package com.example.samplekafkaintegration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaIntegrationProperties(
        String tradeTopic,
        String tradeDltTopic
) {
}
