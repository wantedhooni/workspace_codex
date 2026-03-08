package com.example.samplesaga.order.api;

import com.example.samplesaga.saga.api.OrderSagaResponse;

public record SagaOrderResultResponse(
        SagaOrderResponse order,
        OrderSagaResponse saga
) {
}
