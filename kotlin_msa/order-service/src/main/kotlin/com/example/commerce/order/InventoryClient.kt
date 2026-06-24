package com.example.commerce.order

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

/**
 * 재고 서비스와 통신하는 HTTP 클라이언트입니다.
 */
@FeignClient(name = "inventory-service", url = "\${commerce.clients.inventory-url}")
interface InventoryClient {
    /**
     * 주문 생성을 위해 재고 예약 API를 호출합니다.
     */
    @PostMapping("/api/inventories/reservations")
    fun reserve(@RequestBody request: ReserveInventoryRequest): ReserveInventoryResponse

    /**
     * 주문 실패 시 재고 예약 해제 API를 호출합니다.
     */
    @PostMapping("/api/inventories/reservations/release")
    fun release(@RequestBody request: ReleaseInventoryRequest)
}

