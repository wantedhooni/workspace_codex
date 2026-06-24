package com.example.commerce.payment

import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 결제 승인 시뮬레이션과 결제 이력 조회를 담당하는 서비스입니다.
 *
 * 외부 PG 연동 전 단계로, 주문 금액 검증과 승인/거절 결과 생성을 캡슐화합니다.
 */
@Service
class PaymentService {
    private val payments = ConcurrentHashMap<String, Payment>()

    /**
     * 주문 결제를 승인하고 결제 이력을 저장합니다.
     */
    fun approve(request: ApprovePaymentRequest): ApprovePaymentResponse {
        val paymentId = "PAY-${UUID.randomUUID().toString().take(8)}"
        val declined = request.amount > BigDecimal("1000000")

        val payment = Payment(
            paymentId = paymentId,
            orderId = request.orderId,
            customerId = request.customerId,
            amount = request.amount,
            method = request.method,
            status = if (declined) PaymentStatus.DECLINED else PaymentStatus.APPROVED,
            approvedAt = if (declined) null else Instant.now(),
            failureReason = if (declined) "단일 결제 승인 한도를 초과했습니다." else null,
        )

        payments[paymentId] = payment

        return ApprovePaymentResponse(
            paymentId = payment.paymentId,
            orderId = payment.orderId,
            status = payment.status,
            approved = payment.status == PaymentStatus.APPROVED,
            reason = payment.failureReason,
        )
    }

    /**
     * 결제 식별자 기준으로 결제 이력을 조회합니다.
     */
    fun findById(paymentId: String): Payment =
        payments[paymentId] ?: throw PaymentNotFoundException(paymentId)

    /**
     * 특정 주문의 결제 이력을 조회합니다.
     */
    fun findByOrderId(orderId: String): List<Payment> =
        payments.values.filter { it.orderId == orderId }.sortedByDescending { it.approvedAt }
}

/**
 * 결제 이력을 찾을 수 없을 때 발생하는 예외입니다.
 */
class PaymentNotFoundException(paymentId: String) : RuntimeException("결제 정보를 찾을 수 없습니다. paymentId=$paymentId")

