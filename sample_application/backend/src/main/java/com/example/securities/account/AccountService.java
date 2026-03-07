package com.example.securities.account;

import static com.example.securities.account.QAccount.account;
import static com.example.securities.account.QLedgerEntry.ledgerEntry;

import com.example.securities.account.AccountDtos.AccountResponse;
import com.example.securities.account.AccountDtos.LedgerEntryView;
import com.example.securities.account.AccountDtos.LedgerSearchRequest;
import com.example.securities.account.AccountDtos.TransactionRequest;
import com.example.securities.account.AccountDtos.TransactionResponse;
import com.example.securities.common.BusinessException;
import com.example.securities.information.InformationService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final InformationService informationService;
    private final JPAQueryFactory jpaQueryFactory;

    public AccountService(
            AccountRepository accountRepository,
            LedgerEntryRepository ledgerEntryRepository,
            InformationService informationService,
            JPAQueryFactory jpaQueryFactory
    ) {
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.informationService = informationService;
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Transactional
    public String openAccount(String customerId, String seed) {
        String accountNo = "A" + Math.abs((customerId + seed + System.nanoTime()).hashCode());
        Account account = new Account(customerId, accountNo);
        Account saved = accountRepository.save(account);

        informationService.storeConfirmedEvent(
                "ACCOUNT_OPENED",
                saved.getId(),
                "customerId=" + customerId + ",accountNo=" + accountNo
        );
        return saved.getId();
    }

    @Transactional
    public AccountResponse openAccount(String customerId) {
        String accountId = openAccount(customerId, UUID.randomUUID().toString());
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("ACCOUNT_NOT_FOUND", "계좌를 찾을 수 없습니다."));
        return toResponse(account);
    }

    @Transactional
    public TransactionResponse transact(String accountId, TransactionRequest request) {
        ledgerEntryRepository.findByReferenceId(request.referenceId()).ifPresent(entry -> {
            throw new BusinessException("DUPLICATE_REFERENCE", "이미 처리된 referenceId 입니다.");
        });

        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new BusinessException("ACCOUNT_NOT_FOUND", "계좌를 찾을 수 없습니다."));

        BigDecimal amount = request.amount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "금액은 0보다 커야 합니다.");
        }

        if (request.type() == LedgerType.WITHDRAW && account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE", "잔액이 부족합니다.");
        }

        if (request.type() == LedgerType.DEPOSIT) {
            account.deposit(amount);
        } else {
            account.withdraw(amount);
        }

        LedgerEntry entry = new LedgerEntry(
                account.getId(),
                request.type(),
                amount,
                account.getBalance(),
                request.referenceId()
        );

        LedgerEntry saved = ledgerEntryRepository.save(entry);

        informationService.storeConfirmedEvent(
                "ACCOUNT_TRANSACTION_CONFIRMED",
                account.getId(),
                "type=" + request.type() + ",amount=" + amount + ",balanceAfter=" + account.getBalance()
        );

        return new TransactionResponse(
                saved.getId(),
                saved.getAccountId(),
                saved.getType(),
                saved.getAmount(),
                saved.getBalanceAfter(),
                saved.getReferenceId(),
                saved.getOccurredAt()
        );
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccounts() {
        return accountRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LedgerEntryView> searchLedger(LedgerSearchRequest request) {
        BooleanBuilder where = new BooleanBuilder();

        if (request.accountId() != null && !request.accountId().isBlank()) {
            where.and(ledgerEntry.accountId.eq(request.accountId()));
        }
        if (request.customerId() != null && !request.customerId().isBlank()) {
            where.and(account.customerId.eq(request.customerId()));
        }
        if (request.type() != null) {
            where.and(ledgerEntry.type.eq(request.type()));
        }
        if (request.fromOccurredAt() != null) {
            where.and(ledgerEntry.occurredAt.goe(request.fromOccurredAt()));
        }
        if (request.toOccurredAt() != null) {
            where.and(ledgerEntry.occurredAt.loe(request.toOccurredAt()));
        }
        if (request.minAmount() != null) {
            where.and(ledgerEntry.amount.goe(request.minAmount()));
        }
        if (request.maxAmount() != null) {
            where.and(ledgerEntry.amount.loe(request.maxAmount()));
        }

        int limit = request.limit() == null ? 100 : Math.min(request.limit(), 1000);

        List<Tuple> rows = jpaQueryFactory
                .select(
                        ledgerEntry.id,
                        ledgerEntry.accountId,
                        account.accountNo,
                        account.customerId,
                        ledgerEntry.type,
                        ledgerEntry.amount,
                        ledgerEntry.balanceAfter,
                        ledgerEntry.referenceId,
                        ledgerEntry.occurredAt
                )
                .from(ledgerEntry)
                .join(account).on(account.id.eq(ledgerEntry.accountId))
                .where(where)
                .orderBy(ledgerEntry.occurredAt.desc())
                .limit(limit)
                .fetch();

        return rows.stream()
                .map(row -> new LedgerEntryView(
                        row.get(ledgerEntry.id),
                        row.get(ledgerEntry.accountId),
                        row.get(account.accountNo),
                        row.get(account.customerId),
                        row.get(ledgerEntry.type),
                        row.get(ledgerEntry.amount),
                        row.get(ledgerEntry.balanceAfter),
                        row.get(ledgerEntry.referenceId),
                        row.get(ledgerEntry.occurredAt)
                ))
                .toList();
    }

    private AccountResponse toResponse(Account accountEntity) {
        return new AccountResponse(
                accountEntity.getId(),
                accountEntity.getAccountNo(),
                accountEntity.getCustomerId(),
                accountEntity.getStatus(),
                accountEntity.getBalance(),
                accountEntity.getCreatedAt()
        );
    }
}
