package com.example.transactionaloutbox;

import com.example.transactionaloutbox.config.KafkaTopicsProperties;
import com.example.transactionaloutbox.config.OutboxRelayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Transactional Outbox 샘플 애플리케이션의 진입점이다.
 */
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({KafkaTopicsProperties.class, OutboxRelayProperties.class})
public class TransactionalOutboxApplication {

    /**
     * Spring Boot 애플리케이션을 시작한다.
     *
     * @param args 실행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(TransactionalOutboxApplication.class, args);
    }
}
