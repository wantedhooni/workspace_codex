package com.example.samplecqrs.command.application;

import com.example.samplecqrs.command.api.CreateOrderRequest;
import com.example.samplecqrs.command.api.OrderCommandResponse;
import com.example.samplecqrs.command.domain.PurchaseOrder;
import com.example.samplecqrs.command.domain.PurchaseOrderRepository;
import com.example.samplecqrs.events.OrderCancelledEvent;
import com.example.samplecqrs.events.OrderCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 생성과 취소를 처리하는 쓰기 모델 애플리케이션 서비스다.
 */
@Service
public class OrderCommandService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public OrderCommandService(
            PurchaseOrderRepository purchaseOrderRepository,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * 주문을 생성하고 조회 모델 갱신용 이벤트를 발행한다.
     *
     * @param request 주문 생성 요청
     * @return 생성된 주문 응답
     */
    @Transactional
    public OrderCommandResponse createOrder(CreateOrderRequest request) {
        PurchaseOrder order = purchaseOrderRepository.save(
                new PurchaseOrder(request.customerId(), request.productCode(), request.quantity(), request.unitPrice())
        );
        applicationEventPublisher.publishEvent(OrderCreatedEvent.from(order));
        return OrderCommandResponseMapper.toResponse(order);
    }

    /**
     * 주문을 취소하고 조회 모델 갱신용 이벤트를 발행한다.
     *
     * @param orderId 주문 식별자
     * @return 취소된 주문 응답
     */
    @Transactional
    public OrderCommandResponse cancelOrder(String orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.cancel();
        applicationEventPublisher.publishEvent(OrderCancelledEvent.from(order));
        return OrderCommandResponseMapper.toResponse(order);
    }
}
