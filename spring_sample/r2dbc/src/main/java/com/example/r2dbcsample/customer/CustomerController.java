package com.example.r2dbcsample.customer;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerCommandService customerCommandService;

    public CustomerController(CustomerCommandService customerCommandService) {
        this.customerCommandService = customerCommandService;
    }

    @GetMapping
    public Flux<CustomerResponse> getCustomers() {
        return customerCommandService.getCustomers();
    }

    @GetMapping("/{customerCode}")
    public Mono<CustomerResponse> getCustomer(@PathVariable String customerCode) {
        return customerCommandService.getCustomer(customerCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return customerCommandService.createCustomer(request);
    }

    @PatchMapping("/{customerCode}/tier")
    public Mono<CustomerResponse> changeTier(
            @PathVariable String customerCode,
            @Valid @RequestBody ChangeCustomerTierRequest request
    ) {
        return customerCommandService.changeTier(customerCode, request);
    }
}
