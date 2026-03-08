package com.example.samplesaga.saga.application;

import com.example.samplesaga.inventory.application.InventoryService;
import com.example.samplesaga.order.api.CreateSagaOrderRequest;
import com.example.samplesaga.order.api.SagaOrderResultResponse;
import com.example.samplesaga.order.application.SagaOrderQueryService;
import com.example.samplesaga.order.domain.SagaOrder;
import com.example.samplesaga.order.domain.SagaOrderRepository;
import com.example.samplesaga.payment.application.PaymentService;
import com.example.samplesaga.saga.domain.OrderSaga;
import com.example.samplesaga.saga.domain.OrderSagaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 처리 Saga의 단계 실행과 보상 흐름을 조정한다.
 */
@Service
public class OrderSagaOrchestrator {

    private final SagaOrderRepository sagaOrderRepository;
    private final OrderSagaRepository orderSagaRepository;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final SagaOrderQueryService sagaOrderQueryService;

    public OrderSagaOrchestrator(
            SagaOrderRepository sagaOrderRepository,
            OrderSagaRepository orderSagaRepository,
            PaymentService paymentService,
            InventoryService inventoryService,
            SagaOrderQueryService sagaOrderQueryService
    ) {
        this.sagaOrderRepository = sagaOrderRepository;
        this.orderSagaRepository = orderSagaRepository;
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
        this.sagaOrderQueryService = sagaOrderQueryService;
    }

    /**
     * 주문 생성부터 결제/재고/보상까지 Saga 전체 흐름을 시작한다.
     *
     * @param request 주문 생성 요청
     * @return 주문과 Saga 상태 응답
     */
    @Transactional
    public SagaOrderResultResponse start(CreateSagaOrderRequest request) {
        SagaOrder order = sagaOrderRepository.save(
                new SagaOrder(request.customerId(), request.productCode(), request.quantity(), request.unitPrice())
        );
        OrderSaga saga = orderSagaRepository.save(new OrderSaga(order.getId()));

        try {
            paymentService.approvePayment(order);
            saga.markPaymentCompleted();

            inventoryService.reserveInventory(order);
            saga.markInventoryReserved();

            order.markCompleted();
            saga.markCompleted();
        } catch (Exception paymentOrInventoryFailure) {
            compensate(order, saga, paymentOrInventoryFailure);
        }

        return new SagaOrderResultResponse(
                sagaOrderQueryService.toResponse(order),
                sagaOrderQueryService.toResponse(saga)
        );
    }

    /**
     * 단계 실패에 따라 적절한 보상 또는 실패 상태를 반영한다.
     *
     * @param order 주문 엔티티
     * @param saga Saga 엔티티
     * @param failure 발생한 예외
     */
    private void compensate(SagaOrder order, OrderSaga saga, Exception failure) {
        String reason = failure.getMessage();

        if (saga.getPaymentStatus().name().equals("COMPLETED")) {
            paymentService.cancelPayment(order.getId());
            order.markCompensated(reason);
            saga.markCompensated(reason);
            return;
        }

        order.markFailed(reason);
        saga.markFailed(reason);
    }
}
