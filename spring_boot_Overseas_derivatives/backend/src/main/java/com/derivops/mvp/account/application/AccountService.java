package com.derivops.mvp.account.application;
import com.derivops.mvp.account.*;
import com.derivops.mvp.account.api.*;
import com.derivops.mvp.account.dto.*;
import com.derivops.mvp.account.infrastructure.*;


import com.derivops.mvp.common.NotFoundException;
import com.derivops.mvp.common.SecurityUtils;
import com.derivops.mvp.position.Balance;
import com.derivops.mvp.position.infrastructure.BalanceRepository;
import com.derivops.mvp.position.Margin;
import com.derivops.mvp.position.infrastructure.MarginRepository;
import com.derivops.mvp.position.Position;
import com.derivops.mvp.position.infrastructure.PositionRepository;
import com.derivops.mvp.user.UserRole;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AccountService {
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
        return accountRepository.search(status, broker, keyword, filter, pageable)
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
