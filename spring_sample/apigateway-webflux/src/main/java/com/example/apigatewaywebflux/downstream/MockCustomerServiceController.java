package com.example.apigatewaywebflux.downstream;

import java.time.OffsetDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/mock/customer-service")
public class MockCustomerServiceController {

    @GetMapping("/customers/{customerCode}")
    public Mono<CustomerServiceResponse> getCustomer(@PathVariable String customerCode) {
        return Mono.just(new CustomerServiceResponse(
                customerCode,
                "ACTIVE",
                "vip-segment",
                OffsetDateTime.now()
        ));
    }
}
