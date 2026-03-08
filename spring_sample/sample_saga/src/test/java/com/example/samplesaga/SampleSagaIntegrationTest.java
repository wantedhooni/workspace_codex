package com.example.samplesaga;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.samplesaga.order.api.CreateSagaOrderRequest;
import com.example.samplesaga.order.api.SagaOrderResultResponse;
import com.example.samplesaga.order.application.SagaOrderQueryService;
import com.example.samplesaga.order.domain.SagaOrderStatus;
import com.example.samplesaga.payment.domain.PaymentRecordRepository;
import com.example.samplesaga.payment.domain.PaymentStatus;
import com.example.samplesaga.saga.application.OrderSagaOrchestrator;
import com.example.samplesaga.saga.domain.SagaStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SampleSagaIntegrationTest {

    @Autowired
    private OrderSagaOrchestrator orderSagaOrchestrator;

    @Autowired
    private SagaOrderQueryService sagaOrderQueryService;

    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    @Test
    void 모든_단계가_성공하면_주문과_Saga가_완료된다() {
        SagaOrderResultResponse response = orderSagaOrchestrator.start(
                new CreateSagaOrderRequest("CUST-700", "DESK-01", 1, new BigDecimal("210000"))
        );

        assertThat(response.order().status()).isEqualTo(SagaOrderStatus.COMPLETED);
        assertThat(response.saga().status()).isEqualTo(SagaStatus.COMPLETED);
    }

    @Test
    void 재고_실패시_결제가_보상되고_Saga가_compensated_상태가_된다() {
        SagaOrderResultResponse response = orderSagaOrchestrator.start(
                new CreateSagaOrderRequest("CUST-701", "LIMITED-STOCK", 2, new BigDecimal("99000"))
        );

        var order = sagaOrderQueryService.getOrder(response.order().orderId());
        var saga = sagaOrderQueryService.getSaga(response.order().orderId());
        var payment = paymentRecordRepository.findByOrderId(response.order().orderId()).orElseThrow();

        assertThat(order.status()).isEqualTo(SagaOrderStatus.COMPENSATED);
        assertThat(saga.status()).isEqualTo(SagaStatus.COMPENSATED);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    void 결제_실패시_보상없이_실패_상태가_된다() {
        SagaOrderResultResponse response = orderSagaOrchestrator.start(
                new CreateSagaOrderRequest("FAIL-PAYMENT", "DESK-02", 1, new BigDecimal("150000"))
        );

        var order = sagaOrderQueryService.getOrder(response.order().orderId());
        var saga = sagaOrderQueryService.getSaga(response.order().orderId());

        assertThat(order.status()).isEqualTo(SagaOrderStatus.FAILED);
        assertThat(saga.status()).isEqualTo(SagaStatus.FAILED);
        assertThat(paymentRecordRepository.findByOrderId(response.order().orderId())).isEmpty();
    }
}
