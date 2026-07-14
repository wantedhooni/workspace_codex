package com.example.commerce.contents;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 금융 공지사항과 게시물의 작성, 발행, 조회 기능을 제공하는 Contents Service 애플리케이션이다.
 */
@SpringBootApplication
public class ContentsServiceApplication {

    /**
     * Spring 컨테이너가 애플리케이션 구성 클래스를 생성할 때 사용한다.
     */
    public ContentsServiceApplication() {
    }

    /**
     * Contents Service 애플리케이션을 시작한다.
     *
     * @param args 실행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(ContentsServiceApplication.class, args);
    }

    /**
     * 콘텐츠 생성과 상태 변경 시각을 테스트 가능하게 주입하기 위한 UTC 시계를 제공한다.
     *
     * @return UTC 기준 시스템 시계
     */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
