package com.example.sampleeventdriven;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SampleEventDrivenApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleEventDrivenApplication.class, args);
    }
}
