package com.curd.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.curd.template")
@EnableJpaAuditing
public class CrudFilterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrudFilterApplication.class, args);
    }
}
