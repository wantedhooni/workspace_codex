package com.revy.mvpbanking.account.application;

import com.revy.mvpbanking.account.domain.Account;
import com.revy.mvpbanking.account.domain.AccountStatus;
import com.revy.mvpbanking.account.domain.AccountType;
import com.revy.mvpbanking.account.domain.AdminAccountQueryRepository;
import com.revy.mvpbanking.account.domain.AccountRepository;
import com.revy.mvpbanking.customer.domain.CustomerRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class AccountQueryService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AdminAccountQueryRepository adminAccountQueryRepository;

    public AccountQueryService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            AdminAccountQueryRepository adminAccountQueryRepository
    ) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.adminAccountQueryRepository = adminAccountQueryRepository;
    }

    public Page<Account> getAllAccounts(
            String query,
            AccountStatus status,
            AccountType accountType,
            BigDecimal minBalance,
            BigDecimal maxBalance,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        return adminAccountQueryRepository.search(query, status, accountType, minBalance, maxBalance, sortBy, sortDir, page, size);
    }

    public List<Account> getAccountsForEndUser(UUID endUserId) {
        var customer = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));
        return accountRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
    }
}
