package com.derivops.mvp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.derivops.mvp")
@EnableAsync
@SpringBootApplication
public class DerivativesAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(DerivativesAdminApplication.class, args);
    }
}
