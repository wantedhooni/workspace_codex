package com.example.samplenativeimage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(NativeRuntimeHints.class)
public class SampleNativeImageApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleNativeImageApplication.class, args);
    }
}
