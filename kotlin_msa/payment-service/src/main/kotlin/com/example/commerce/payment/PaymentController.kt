package com.example.commerce.payment

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
 * 결제 승인과 결제 조회 API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentService: PaymentService,
) {
    /**
     * 주문 결제를 승인합니다.
     */
    @PostMapping("/approvals")
    @ResponseStatus(HttpStatus.CREATED)
    fun approve(@Valid @RequestBody request: ApprovePaymentRequest): ApprovePaymentResponse =
        paymentService.approve(request)

    /**
     * 결제 식별자 기준으로 결제 이력을 조회합니다.
     */
    @GetMapping("/{paymentId}")
    fun findById(@PathVariable paymentId: String): Payment =
        paymentService.findById(paymentId)

    /**
     * 주문 식별자 기준으로 결제 이력을 조회합니다.
     */
    @GetMapping("/orders/{orderId}")
    fun findByOrderId(@PathVariable orderId: String): List<Payment> =
        paymentService.findByOrderId(orderId)

    /**
     * 결제 도메인 예외를 HTTP 응답으로 변환합니다.
     */
    @ExceptionHandler(PaymentNotFoundException::class)
    fun handleNotFound(ex: PaymentNotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to ex.message.orEmpty()))
}

