package com.example.commerce.order

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

/**
 * 결제 서비스와 통신하는 HTTP 클라이언트입니다.
 */
@FeignClient(name = "payment-service", url = "\${commerce.clients.payment-url}")
interface PaymentClient {
    /**
     * 주문 금액에 대한 결제 승인 API를 호출합니다.
     */
    @PostMapping("/api/payments/approvals")
    fun approve(@RequestBody request: ApprovePaymentRequest): ApprovePaymentResponse
}

