package com.example.websample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/** Admin 주체 API 서버의 부트스트랩 클래스입니다. */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class AdminServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServerApplication.class, args);
    }
}
