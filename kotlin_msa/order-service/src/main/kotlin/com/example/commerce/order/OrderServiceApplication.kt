package com.example.commerce.order

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

/**
 * 주문 서비스 애플리케이션의 진입점입니다.
 *
 * 주문 생성 과정에서 재고 서비스와 결제 서비스를 호출합니다.
 */
@EnableFeignClients
@SpringBootApplication
class OrderServiceApplication

fun main(args: Array<String>) {
    runApplication<OrderServiceApplication>(*args)
}

