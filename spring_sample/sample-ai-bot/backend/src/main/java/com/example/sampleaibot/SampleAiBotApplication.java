package com.example.sampleaibot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SampleAiBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleAiBotApplication.class, args);
    }
}
