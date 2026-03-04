package com.example.r2dbcsample.customer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CustomerAccountRepository extends ReactiveCrudRepository<CustomerAccount, Long> {

    Mono<Boolean> existsByCustomerCode(String customerCode);

    Mono<CustomerAccount> findByCustomerCode(String customerCode);

    Flux<CustomerAccount> findAllByOrderByCreatedAtDesc();
}
