package com.example.transactionaloutbox;

import com.example.transactionaloutbox.config.KafkaTopicsProperties;
import com.example.transactionaloutbox.config.OutboxRelayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({KafkaTopicsProperties.class, OutboxRelayProperties.class})
public class TransactionalOutboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionalOutboxApplication.class, args);
    }
}
