package com.example.r2dbcsample.customer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.stereotype.Service;

@Service
public class CustomerCommandService {

    private final CustomerAccountRepository customerAccountRepository;

    public CustomerCommandService(CustomerAccountRepository customerAccountRepository) {
        this.customerAccountRepository = customerAccountRepository;
    }

    public Flux<CustomerResponse> getCustomers() {
        return customerAccountRepository.findAllByOrderByCreatedAtDesc()
                .map(CustomerResponse::from);
    }

    public Mono<CustomerResponse> getCustomer(String customerCode) {
        return customerAccountRepository.findByCustomerCode(customerCode)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(customerCode)))
                .map(CustomerResponse::from);
    }

    public Mono<CustomerResponse> createCustomer(CreateCustomerRequest request) {
        return customerAccountRepository.existsByCustomerCode(request.customerCode())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateCustomerCodeException(request.customerCode()));
                    }
                    CustomerAccount customerAccount = CustomerAccount.create(
                            request.customerCode(),
                            request.name(),
                            request.email(),
                            request.tier()
                    );
                    return customerAccountRepository.save(customerAccount);
                })
                .map(CustomerResponse::from);
    }

    public Mono<CustomerResponse> changeTier(String customerCode, ChangeCustomerTierRequest request) {
        return customerAccountRepository.findByCustomerCode(customerCode)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(customerCode)))
                .map(customerAccount -> customerAccount.withTier(request.tier()))
                .flatMap(customerAccountRepository::save)
                .map(CustomerResponse::from);
    }
}
