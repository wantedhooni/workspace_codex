package com.example.apigatewaywebflux.downstream;

import java.time.OffsetDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/mock/order-service")
public class MockOrderServiceController {

    @GetMapping("/orders/{orderNumber}")
    public Mono<OrderServiceResponse> getOrder(@PathVariable String orderNumber) {
        return Mono.just(new OrderServiceResponse(
                orderNumber,
                "FULFILLING",
                245000,
                OffsetDateTime.now()
        ));
    }
}
