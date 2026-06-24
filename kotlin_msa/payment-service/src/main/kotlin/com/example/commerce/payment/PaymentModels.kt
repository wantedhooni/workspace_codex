package com.example.commerce.payment

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.time.Instant

/**
 * 결제 승인 이력을 표현합니다.
 */
data class Payment(
    val paymentId: String,
    val orderId: String,
    val customerId: String,
    val amount: BigDecimal,
    val method: PaymentMethod,
    val status: PaymentStatus,
    val approvedAt: Instant?,
    val failureReason: String? = null,
)

/**
 * 결제 수단을 정의합니다.
 */
enum class PaymentMethod {
    CARD,
    BANK_TRANSFER,
    POINT,
}

/**
 * 결제 처리 상태를 정의합니다.
 */
enum class PaymentStatus {
    APPROVED,
    DECLINED,
}

/**
 * 결제 승인 요청 본문입니다.
 */
data class ApprovePaymentRequest(
    @field:NotBlank val orderId: String,
    @field:NotBlank val customerId: String,
    @field:DecimalMin("0.0", inclusive = false) val amount: BigDecimal,
    val method: PaymentMethod,
)

/**
 * 결제 승인 응답 본문입니다.
 */
data class ApprovePaymentResponse(
    val paymentId: String,
    val orderId: String,
    val status: PaymentStatus,
    val approved: Boolean,
    val reason: String? = null,
)

