package com.example.commerce.payment

import io.cucumber.java.Before
import io.cucumber.java.ko.그리고
import io.cucumber.java.ko.그러면
import io.cucumber.java.ko.만일
import io.cucumber.java.ko.조건
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import java.math.BigDecimal

class PaymentStepDefinitions {
    private lateinit var paymentService: PaymentService
    private lateinit var request: ApprovePaymentRequest
    private var response: ApprovePaymentResponse? = null
    private var payments: List<Payment> = emptyList()
    private var targetPaymentId: String = ""

    @Before
    fun setUp() {
        paymentService = PaymentService()
        response = null
        payments = emptyList()
        targetPaymentId = ""
    }

    @조건("주문 {string} 의 결제 금액이 {long}원이다")
    fun orderPaymentAmountIs(orderId: String, amount: Long) {
        request = ApprovePaymentRequest(orderId, "CUST-001", BigDecimal(amount), PaymentMethod.CARD)
    }

    @만일("카드 결제를 승인한다")
    fun approveCardPayment() {
        response = paymentService.approve(request)
    }

    @그러면("결제 상태는 {string} 이다")
    fun paymentStatusShouldBe(status: String) {
        assertThat(response?.status).isEqualTo(PaymentStatus.valueOf(status))
    }

    @그리고("결제 이력은 승인 시각을 가진다")
    fun paymentHistoryShouldHaveApprovedAt() {
        val payment = paymentService.findById(response!!.paymentId)
        assertThat(payment.approvedAt).isNotNull()
    }

    @그리고("결제 거절 사유에는 {string} 가 포함된다")
    fun paymentReasonShouldContain(reason: String) {
        assertThat(response?.reason).contains(reason)
    }

    @조건("주문 {string} 의 결제 이력이 저장되어 있다")
    fun paymentHistoryExists(orderId: String) {
        paymentService.approve(ApprovePaymentRequest(orderId, "CUST-001", BigDecimal("10000"), PaymentMethod.CARD))
    }

    @만일("주문 ID {string} 로 결제 이력을 조회한다")
    fun findPaymentsByOrderId(orderId: String) {
        payments = paymentService.findByOrderId(orderId)
    }

    @그러면("조회된 결제 이력은 {int}건이다")
    fun paymentHistoryCountShouldBe(count: Int) {
        assertThat(payments).hasSize(count)
    }

    @조건("존재하지 않는 결제 ID {string} 이 있다")
    fun unknownPaymentIdExists(paymentId: String) {
        targetPaymentId = paymentId
    }

    @만일("해당 결제를 조회한다")
    fun findUnknownPayment() {
    }

    @그러면("결제 없음 예외가 발생한다")
    fun paymentNotFoundExceptionShouldBeThrown() {
        assertThatThrownBy { paymentService.findById(targetPaymentId) }
            .isInstanceOf(PaymentNotFoundException::class.java)
            .hasMessageContaining(targetPaymentId)
    }
}

