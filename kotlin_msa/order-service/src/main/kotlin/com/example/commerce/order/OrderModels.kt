package com.example.commerce.order

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.math.BigDecimal
import java.time.Instant

/**
 * 고객 주문의 현재 처리 상태를 표현합니다.
 */
data class Order(
    val orderId: String,
    val customerId: String,
    val items: List<OrderLine>,
    val totalAmount: BigDecimal,
    val paymentMethod: PaymentMethod,
    val paymentId: String?,
    val status: OrderStatus,
    val createdAt: Instant,
    val failureReason: String? = null,
)

/**
 * 주문 상품 라인을 표현합니다.
 */
data class OrderLine(
    val sku: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
)

/**
 * 주문 처리 상태를 정의합니다.
 */
enum class OrderStatus {
    CREATED,
    FAILED,
}

/**
 * 주문에서 사용할 결제 수단을 정의합니다.
 */
enum class PaymentMethod {
    CARD,
    BANK_TRANSFER,
    POINT,
}

/**
 * 주문 생성 요청 본문입니다.
 */
data class CreateOrderRequest(
    @field:NotBlank val customerId: String,
    @field:NotEmpty @field:Valid val items: List<CreateOrderLineRequest>,
    val paymentMethod: PaymentMethod,
)

/**
 * 주문 생성 상품 라인 요청 본문입니다.
 */
data class CreateOrderLineRequest(
    @field:NotBlank val sku: String,
    @field:Min(1) val quantity: Int,
    @field:DecimalMin("0.0", inclusive = false) val unitPrice: BigDecimal,
)

/**
 * 재고 예약 요청 DTO입니다.
 */
data class ReserveInventoryRequest(
    val orderId: String,
    val sku: String,
    val quantity: Int,
)

/**
 * 재고 예약 응답 DTO입니다.
 */
data class ReserveInventoryResponse(
    val orderId: String,
    val sku: String,
    val reservedQuantity: Int,
    val accepted: Boolean,
    val reason: String? = null,
)

/**
 * 재고 예약 해제 요청 DTO입니다.
 */
data class ReleaseInventoryRequest(
    val orderId: String,
    val sku: String,
    val quantity: Int,
)

/**
 * 결제 승인 요청 DTO입니다.
 */
data class ApprovePaymentRequest(
    val orderId: String,
    val customerId: String,
    val amount: BigDecimal,
    val method: PaymentMethod,
)

/**
 * 결제 승인 응답 DTO입니다.
 */
data class ApprovePaymentResponse(
    val paymentId: String,
    val orderId: String,
    val status: String,
    val approved: Boolean,
    val reason: String? = null,
)

