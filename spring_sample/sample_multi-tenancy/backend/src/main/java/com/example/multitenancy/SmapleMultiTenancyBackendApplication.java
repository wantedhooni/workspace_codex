package com.example.multitenancy;

import com.example.multitenancy.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * JWT 기반 멀티테넌시 데모 서버를 시작한다.
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class SmapleMultiTenancyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmapleMultiTenancyBackendApplication.class, args);
    }
}
