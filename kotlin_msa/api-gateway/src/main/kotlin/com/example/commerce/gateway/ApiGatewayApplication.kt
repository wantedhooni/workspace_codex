package com.example.commerce.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * API Gateway 애플리케이션의 진입점입니다.
 *
 * 외부 요청을 커머스 마이크로서비스 내부 API로 라우팅합니다.
 */
@SpringBootApplication
class ApiGatewayApplication

fun main(args: Array<String>) {
    runApplication<ApiGatewayApplication>(*args)
}

