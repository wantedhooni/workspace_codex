package com.example.commerce.inventory

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * 재고 서비스 애플리케이션의 진입점입니다.
 *
 * 상품 SKU별 가용 재고와 예약 재고를 독립적으로 관리합니다.
 */
@SpringBootApplication
class InventoryServiceApplication

fun main(args: Array<String>) {
    runApplication<InventoryServiceApplication>(*args)
}

