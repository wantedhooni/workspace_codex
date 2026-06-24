package com.example.commerce.order

import io.cucumber.java.Before
import io.cucumber.java.ko.그리고
import io.cucumber.java.ko.그러면
import io.cucumber.java.ko.만일
import io.cucumber.java.ko.조건
import org.assertj.core.api.Assertions.assertThat
import java.math.BigDecimal

class OrderStepDefinitions {
    private lateinit var inventoryClient: RecordingInventoryClient
    private lateinit var paymentClient: RecordingPaymentClient
    private lateinit var orderService: OrderService
    private var order: Order? = null
    private var creationException: OrderCreationException? = null

    @Before
    fun setUp() {
        inventoryClient = RecordingInventoryClient()
        paymentClient = RecordingPaymentClient()
        orderService = OrderService(inventoryClient, paymentClient)
        order = null
        creationException = null
    }

    @조건("재고 예약이 성공하도록 준비되어 있다")
    fun inventoryReservationWillSucceed() {
        inventoryClient.reservationAccepted = true
    }

    @조건("재고 예약이 실패하도록 준비되어 있다")
    fun inventoryReservationWillFail() {
        inventoryClient.reservationAccepted = false
    }

    @그리고("결제 승인이 성공하도록 준비되어 있다")
    fun paymentApprovalWillSucceed() {
        paymentClient.paymentApproved = true
    }

    @그리고("결제 승인이 실패하도록 준비되어 있다")
    fun paymentApprovalWillFail() {
        paymentClient.paymentApproved = false
    }

    @만일("고객 {string} 이 SKU {string} 상품 {int}개를 주문한다")
    fun customerCreatesOrder(customerId: String, sku: String, quantity: Int) {
        try {
            order = orderService.create(
                CreateOrderRequest(
                    customerId,
                    listOf(CreateOrderLineRequest(sku, quantity, BigDecimal("129000"))),
                    PaymentMethod.CARD,
                ),
            )
        } catch (ex: OrderCreationException) {
            creationException = ex
        }
    }

    @그러면("주문 상태는 {string} 이다")
    fun orderStatusShouldBe(status: String) {
        assertThat(order?.status).isEqualTo(OrderStatus.valueOf(status))
    }

    @그리고("결제 ID가 주문에 저장된다")
    fun paymentIdShouldBeSaved() {
        assertThat(order?.paymentId).isEqualTo("PAY-001")
    }

    @그러면("주문 생성 예외가 발생한다")
    fun orderCreationExceptionShouldBeThrown() {
        assertThat(creationException).isNotNull()
    }

    @그리고("결제 서비스는 호출되지 않는다")
    fun paymentServiceShouldNotBeCalled() {
        assertThat(paymentClient.approveRequests).isEmpty()
    }

    @그리고("실패 주문이 저장된다")
    fun failedOrderShouldBeSaved() {
        val orders = orderService.findAll()
        assertThat(orders).hasSize(1)
        assertThat(orders.single().status).isEqualTo(OrderStatus.FAILED)
        assertThat(orders.single().paymentId).isNull()
    }

    @그리고("예약 재고가 해제된다")
    fun reservedInventoryShouldBeReleased() {
        assertThat(inventoryClient.releasedRequests).hasSize(1)
        assertThat(inventoryClient.releasedRequests.single().sku).isEqualTo("SKU-001")
        assertThat(inventoryClient.releasedRequests.single().quantity).isEqualTo(2)
    }

    private class RecordingInventoryClient : InventoryClient {
        var reservationAccepted: Boolean = true
        val reservedRequests = mutableListOf<ReserveInventoryRequest>()
        val releasedRequests = mutableListOf<ReleaseInventoryRequest>()

        override fun reserve(request: ReserveInventoryRequest): ReserveInventoryResponse {
            reservedRequests += request
            return ReserveInventoryResponse(
                orderId = request.orderId,
                sku = request.sku,
                reservedQuantity = if (reservationAccepted) request.quantity else 0,
                accepted = reservationAccepted,
                reason = if (reservationAccepted) null else "재고 예약에 실패했습니다.",
            )
        }

        override fun release(request: ReleaseInventoryRequest) {
            releasedRequests += request
        }
    }

    private class RecordingPaymentClient : PaymentClient {
        var paymentApproved: Boolean = true
        val approveRequests = mutableListOf<ApprovePaymentRequest>()

        override fun approve(request: ApprovePaymentRequest): ApprovePaymentResponse {
            approveRequests += request
            return ApprovePaymentResponse(
                paymentId = "PAY-001",
                orderId = request.orderId,
                status = if (paymentApproved) "APPROVED" else "DECLINED",
                approved = paymentApproved,
                reason = if (paymentApproved) null else "결제 승인에 실패했습니다.",
            )
        }
    }
}
