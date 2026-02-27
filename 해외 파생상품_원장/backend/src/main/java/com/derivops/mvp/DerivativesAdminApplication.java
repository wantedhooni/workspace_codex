package com.derivops.mvp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DerivativesAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(DerivativesAdminApplication.class, args);
    }
}
