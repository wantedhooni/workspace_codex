package com.example.commerce.account;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 금융 계좌의 개설, 조회, 입출금 원장 기능을 제공하는 Account Service 애플리케이션이다.
 */
@SpringBootApplication
public class AccountServiceApplication {

    /**
     * Spring 컨테이너가 애플리케이션 구성 클래스를 생성할 때 사용한다.
     */
    public AccountServiceApplication() {
    }

    /**
     * Account Service 애플리케이션을 시작한다.
     *
     * @param args 실행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }

    /**
     * 계좌 거래 시각을 테스트 가능하게 주입하기 위한 UTC 시계를 제공한다.
     *
     * @return UTC 기준 시스템 시계
     */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
