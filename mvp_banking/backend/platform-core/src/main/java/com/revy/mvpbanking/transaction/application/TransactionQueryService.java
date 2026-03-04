package com.revy.mvpbanking.transaction.application;

import com.revy.mvpbanking.account.domain.AccountRepository;
import com.revy.mvpbanking.customer.domain.CustomerRepository;
import com.revy.mvpbanking.transaction.domain.AdminTransactionQueryRepository;
import com.revy.mvpbanking.transaction.domain.TransactionEntry;
import com.revy.mvpbanking.transaction.domain.TransactionEntryRepository;
import com.revy.mvpbanking.transaction.domain.TransactionStatus;
import com.revy.mvpbanking.transaction.domain.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class TransactionQueryService {

    private final TransactionEntryRepository transactionEntryRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final AdminTransactionQueryRepository adminTransactionQueryRepository;

    public TransactionQueryService(
            TransactionEntryRepository transactionEntryRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            AdminTransactionQueryRepository adminTransactionQueryRepository
    ) {
        this.transactionEntryRepository = transactionEntryRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.adminTransactionQueryRepository = adminTransactionQueryRepository;
    }

    public Page<TransactionEntry> getAllTransactions(
            String query,
            TransactionStatus status,
            TransactionType type,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Instant occurredFrom,
            Instant occurredTo,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        return adminTransactionQueryRepository.search(
                query,
                status,
                type,
                minAmount,
                maxAmount,
                occurredFrom,
                occurredTo,
                sortBy,
                sortDir,
                page,
                size
        );
    }

    public List<TransactionEntry> getTransactionsForEndUser(UUID endUserId) {
        var customer = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));
        var accounts = accountRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
        return transactionEntryRepository.findByAccountIdInOrderByOccurredAtDesc(accounts.stream().map(account -> account.getId()).toList());
    }
}
