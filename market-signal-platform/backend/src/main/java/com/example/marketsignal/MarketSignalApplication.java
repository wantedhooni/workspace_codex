package com.example.marketsignal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 시장 시그널 플랫폼 백엔드 애플리케이션을 구동한다.
 */
@EnableCaching
@SpringBootApplication
public class MarketSignalApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketSignalApplication.class, args);
    }
}
