package com.derivops.mvp.account;

import com.derivops.mvp.common.NotFoundException;
import com.derivops.mvp.common.SecurityUtils;
import com.derivops.mvp.common.rsql.RsqlSpecificationBuilder;
import com.derivops.mvp.position.Balance;
import com.derivops.mvp.position.BalanceRepository;
import com.derivops.mvp.position.Margin;
import com.derivops.mvp.position.MarginRepository;
import com.derivops.mvp.position.Position;
import com.derivops.mvp.position.PositionRepository;
import com.derivops.mvp.user.UserRole;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AccountService {
    private static final Map<String, String> ACCOUNT_FILTER_FIELDS = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("accountNo", "accountNo"),
            Map.entry("broker", "broker"),
            Map.entry("status", "status"),
            Map.entry("ownerName", "ownerName"),
            Map.entry("openedAt", "openedAt"),
            Map.entry("closedAt", "closedAt")
    );


    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final PositionRepository positionRepository;
    private final MarginRepository marginRepository;

    public Page<AccountListItemResponse> getAccounts(
            AccountStatus status,
            String broker,
            String keyword,
            String filter,
            Pageable pageable,
            boolean unmask
    ) {
        boolean canUnmask = unmask && SecurityUtils.currentRole().filter(role -> role == UserRole.OPS_ADMIN).isPresent();
        Specification<Account> spec = Specification.where(null);
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (broker != null && !broker.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("broker")), broker.trim().toLowerCase()));
        }
        if (keyword != null && !keyword.isBlank()) {
            String q = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("accountNo")), q),
                    cb.like(cb.lower(root.get("ownerName")), q),
                    cb.like(cb.lower(root.get("broker")), q)
            ));
        }
        if (filter != null && !filter.isBlank()) {
            spec = spec.and(RsqlSpecificationBuilder.build(filter, ACCOUNT_FILTER_FIELDS));
        }

        return accountRepository.findAll(spec, pageable)
                .map(account -> new AccountListItemResponse(
                        account.getId(),
                        canUnmask ? account.getAccountNo() : mask(account.getAccountNo()),
                        account.getBroker(),
                        account.getStatus(),
                        account.getOwnerName(),
                        account.getOpenedAt(),
                        account.getClosedAt()
                ));
    }

    public AccountSummaryResponse getSummary(Long accountId, boolean unmask) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        boolean canUnmask = unmask && SecurityUtils.currentRole().filter(role -> role == UserRole.OPS_ADMIN).isPresent();

        Balance latestBalance = balanceRepository.findFirstByAccountIdOrderByTradingDateDesc(accountId);
        Position latestPosition = positionRepository.findFirstByAccountIdOrderByTradingDateDesc(accountId);
        Margin latestMargin = marginRepository.findFirstByAccountIdOrderByTradingDateDesc(accountId);

        LocalDate snapshotDate = LocalDate.now();
        if (latestBalance != null) {
            snapshotDate = latestBalance.getTradingDate();
        } else if (latestPosition != null) {
            snapshotDate = latestPosition.getTradingDate();
        } else if (latestMargin != null) {
            snapshotDate = latestMargin.getTradingDate();
        }

        List<AccountSummaryResponse.BalanceItem> balances = balanceRepository.findByAccountIdAndTradingDate(accountId, snapshotDate)
                .stream()
                .map(b -> new AccountSummaryResponse.BalanceItem(b.getCurrency(), b.getAmount()))
                .toList();

        List<AccountSummaryResponse.PositionItem> positions = positionRepository.findByAccountIdAndTradingDate(accountId, snapshotDate)
                .stream()
                .map(p -> new AccountSummaryResponse.PositionItem(p.getSymbol(), p.getQuantity(), p.getAvgPrice()))
                .toList();

        AccountSummaryResponse.MarginItem margin = latestMargin == null ? null : new AccountSummaryResponse.MarginItem(
                latestMargin.getInitialMargin(),
                latestMargin.getMaintenanceMargin(),
                latestMargin.getAvailableMargin()
        );

        return new AccountSummaryResponse(
                account.getId(),
                canUnmask ? account.getAccountNo() : mask(account.getAccountNo()),
                account.getBroker(),
                account.getStatus(),
                snapshotDate,
                balances,
                positions,
                margin
        );
    }

    private String mask(String accountNo) {
        if (accountNo == null || accountNo.length() < 4) {
            return "****";
        }
        int stars = Math.max(0, accountNo.length() - 4);
        return "*".repeat(stars) + accountNo.substring(accountNo.length() - 4);
    }
}
