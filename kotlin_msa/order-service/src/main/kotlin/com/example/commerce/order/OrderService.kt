package com.example.commerce.order

import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 주문 생성과 주문 조회 비즈니스 규칙을 처리하는 서비스입니다.
 *
 * 재고 예약과 결제 승인을 순차 처리하고 실패 시 예약 재고를 해제합니다.
 */
@Service
class OrderService(
    private val inventoryClient: InventoryClient,
    private val paymentClient: PaymentClient,
) {
    private val orders = ConcurrentHashMap<String, Order>()

    /**
     * 주문을 생성하고 재고 예약 및 결제 승인을 완료합니다.
     */
    fun create(request: CreateOrderRequest): Order {
        val orderId = "ORD-${UUID.randomUUID().toString().take(8)}"
        val lines = request.items.map { OrderLine(it.sku, it.quantity, it.unitPrice) }
        val totalAmount = lines.fold(BigDecimal.ZERO) { sum, line ->
            sum + line.unitPrice.multiply(BigDecimal(line.quantity))
        }
        val reservedLines = mutableListOf<OrderLine>()

        try {
            lines.forEach { line ->
                val reservation = inventoryClient.reserve(
                    ReserveInventoryRequest(orderId = orderId, sku = line.sku, quantity = line.quantity),
                )
                if (!reservation.accepted) {
                    throw OrderCreationException(reservation.reason ?: "재고 예약에 실패했습니다.")
                }
                reservedLines += line
            }

            val payment = paymentClient.approve(
                ApprovePaymentRequest(
                    orderId = orderId,
                    customerId = request.customerId,
                    amount = totalAmount,
                    method = request.paymentMethod,
                ),
            )

            if (!payment.approved) {
                throw OrderCreationException(payment.reason ?: "결제 승인에 실패했습니다.")
            }

            val order = Order(
                orderId = orderId,
                customerId = request.customerId,
                items = lines,
                totalAmount = totalAmount,
                paymentMethod = request.paymentMethod,
                paymentId = payment.paymentId,
                status = OrderStatus.CREATED,
                createdAt = Instant.now(),
            )
            orders[order.orderId] = order
            return order
        } catch (ex: RuntimeException) {
            releaseReservedInventory(orderId, reservedLines)
            val failedOrder = Order(
                orderId = orderId,
                customerId = request.customerId,
                items = lines,
                totalAmount = totalAmount,
                paymentMethod = request.paymentMethod,
                paymentId = null,
                status = OrderStatus.FAILED,
                createdAt = Instant.now(),
                failureReason = ex.message,
            )
            orders[failedOrder.orderId] = failedOrder
            throw OrderCreationException(ex.message ?: "주문 생성에 실패했습니다.")
        }
    }

    /**
     * 모든 주문을 최신순으로 조회합니다.
     */
    fun findAll(): List<Order> =
        orders.values.sortedByDescending { it.createdAt }

    /**
     * 주문 식별자 기준으로 주문을 조회합니다.
     */
    fun findById(orderId: String): Order =
        orders[orderId] ?: throw OrderNotFoundException(orderId)

    private fun releaseReservedInventory(orderId: String, reservedLines: List<OrderLine>) {
        reservedLines.forEach { line ->
            runCatching {
                inventoryClient.release(
                    ReleaseInventoryRequest(orderId = orderId, sku = line.sku, quantity = line.quantity),
                )
            }
        }
    }
}

/**
 * 주문 생성 중 업무 실패가 발생했을 때 사용하는 예외입니다.
 */
class OrderCreationException(message: String) : RuntimeException(message)

/**
 * 주문 정보를 찾을 수 없을 때 발생하는 예외입니다.
 */
class OrderNotFoundException(orderId: String) : RuntimeException("주문을 찾을 수 없습니다. orderId=$orderId")

