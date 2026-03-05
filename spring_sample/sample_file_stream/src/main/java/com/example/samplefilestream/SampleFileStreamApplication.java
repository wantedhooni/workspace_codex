package com.example.samplefilestream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SampleFileStreamApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleFileStreamApplication.class, args);
    }
}
