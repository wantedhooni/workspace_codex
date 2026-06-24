package com.example.commerce.inventory

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 재고 조회와 예약 API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/inventories")
class InventoryController(
    private val inventoryService: InventoryService,
) {
    /**
     * 전체 재고 목록을 조회합니다.
     */
    @GetMapping
    fun findAll(): List<Inventory> =
        inventoryService.findAll()

    /**
     * SKU 기준 재고를 조회합니다.
     */
    @GetMapping("/{sku}")
    fun findBySku(@PathVariable sku: String): Inventory =
        inventoryService.findBySku(sku)

    /**
     * 주문에 필요한 재고를 예약합니다.
     */
    @PostMapping("/reservations")
    fun reserve(@Valid @RequestBody request: ReserveInventoryRequest): ReserveInventoryResponse =
        inventoryService.reserve(request)

    /**
     * 예약된 재고를 해제합니다.
     */
    @PostMapping("/reservations/release")
    fun release(@Valid @RequestBody request: ReleaseInventoryRequest): Inventory =
        inventoryService.release(request)

    /**
     * 재고 도메인 예외를 HTTP 응답으로 변환합니다.
     */
    @ExceptionHandler(InventoryNotFoundException::class)
    fun handleNotFound(ex: InventoryNotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to ex.message.orEmpty()))
}

