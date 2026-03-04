package com.example.samplespringadmin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer
public class SampleSpringAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleSpringAdminApplication.class, args);
    }
}
