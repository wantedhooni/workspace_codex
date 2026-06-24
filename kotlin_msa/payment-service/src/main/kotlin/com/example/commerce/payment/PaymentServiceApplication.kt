package com.example.commerce.payment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * 결제 서비스 애플리케이션의 진입점입니다.
 *
 * 주문 결제 승인과 결제 이력을 독립 마이크로서비스로 관리합니다.
 */
@SpringBootApplication
class PaymentServiceApplication

fun main(args: Array<String>) {
    runApplication<PaymentServiceApplication>(*args)
}

