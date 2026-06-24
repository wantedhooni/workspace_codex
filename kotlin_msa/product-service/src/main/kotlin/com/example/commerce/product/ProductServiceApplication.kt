package com.example.commerce.product

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * 상품 서비스 애플리케이션의 진입점입니다.
 *
 * 커머스 상품 카탈로그를 독립 마이크로서비스로 실행합니다.
 */
@SpringBootApplication
class ProductServiceApplication

fun main(args: Array<String>) {
    runApplication<ProductServiceApplication>(*args)
}

