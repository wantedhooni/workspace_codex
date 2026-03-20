package com.example.observability;

import com.example.observability.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 주문 운영 API와 관측성 스택을 함께 제공하는 샘플 애플리케이션 진입점이다.
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class SampleObservabilityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleObservabilityApplication.class, args);
    }
}
