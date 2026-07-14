package com.example.commerce.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 커머스 외부 요청의 단일 진입점을 제공하는 API Gateway 애플리케이션이다.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    /**
     * Spring 컨테이너가 애플리케이션 구성 클래스를 생성할 때 사용한다.
     */
    public ApiGatewayApplication() {
    }

    /**
     * API Gateway 애플리케이션을 시작한다.
     *
     * @param args 실행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
