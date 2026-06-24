package com.example.commerce.order

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 주문 생성과 주문 조회 API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
) {
    /**
     * 전체 주문 목록을 조회합니다.
     */
    @GetMapping
    fun findAll(): List<Order> =
        orderService.findAll()

    /**
     * 주문 식별자 기준으로 단일 주문을 조회합니다.
     */
    @GetMapping("/{orderId}")
    fun findById(@PathVariable orderId: String): Order =
        orderService.findById(orderId)

    /**
     * 신규 주문을 생성합니다.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateOrderRequest): Order =
        orderService.create(request)

    /**
     * 주문 조회 실패 예외를 HTTP 응답으로 변환합니다.
     */
    @ExceptionHandler(OrderNotFoundException::class)
    fun handleNotFound(ex: OrderNotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to ex.message.orEmpty()))

    /**
     * 주문 생성 실패 예외를 HTTP 응답으로 변환합니다.
     */
    @ExceptionHandler(OrderCreationException::class)
    fun handleCreationFailure(ex: OrderCreationException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("message" to ex.message.orEmpty()))
}

