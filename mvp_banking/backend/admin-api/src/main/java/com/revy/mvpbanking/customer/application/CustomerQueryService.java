package com.revy.mvpbanking.customer.application;

import com.revy.mvpbanking.customer.domain.AdminCustomerQueryRepository;
import com.revy.mvpbanking.customer.domain.Customer;
import com.revy.mvpbanking.customer.domain.CustomerStatus;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerQueryService {

    private final AdminCustomerQueryRepository adminCustomerQueryRepository;

    public CustomerQueryService(AdminCustomerQueryRepository adminCustomerQueryRepository) {
        this.adminCustomerQueryRepository = adminCustomerQueryRepository;
    }

    public Page<Customer> getCustomers(
            String query,
            CustomerStatus status,
            Instant createdFrom,
            Instant createdTo,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        return adminCustomerQueryRepository.search(query, status, createdFrom, createdTo, sortBy, sortDir, page, size);
    }

    public AdminCustomerQueryRepository.CustomerStatusSummary getCustomerSummary(String query, Instant createdFrom, Instant createdTo) {
        return adminCustomerQueryRepository.summarize(query, createdFrom, createdTo);
    }
}
