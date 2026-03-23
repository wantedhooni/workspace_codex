package com.example.samplegoogleoauth;

import com.example.samplegoogleoauth.auth.config.AppAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Google OAuth 데모 애플리케이션의 진입점이다.
 */
@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = AppAuthProperties.class)
public class SampleGoogleOauthApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleGoogleOauthApplication.class, args);
    }
}
