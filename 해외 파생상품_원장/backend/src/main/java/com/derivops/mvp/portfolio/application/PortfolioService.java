package com.derivops.mvp.portfolio.application;

import com.derivops.mvp.account.Account;
import com.derivops.mvp.account.infrastructure.AccountRepository;
import com.derivops.mvp.common.NotFoundException;
import com.derivops.mvp.common.SecurityUtils;
import com.derivops.mvp.portfolio.dto.PortfolioOverviewResponse;
import com.derivops.mvp.position.Balance;
import com.derivops.mvp.position.infrastructure.BalanceRepository;
import com.derivops.mvp.stockposition.StockPosition;
import com.derivops.mvp.stockposition.infrastructure.StockPositionRepository;
import com.derivops.mvp.stockpurchase.StockPurchase;
import com.derivops.mvp.stockpurchase.infrastructure.StockPurchaseRepository;
import com.derivops.mvp.user.UserRole;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PortfolioService {

    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final StockPositionRepository stockPositionRepository;
    private final StockPurchaseRepository stockPurchaseRepository;

    @Transactional(readOnly = true)
    public PortfolioOverviewResponse getOverview(Long accountId, boolean unmask) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        boolean canUnmask = unmask && SecurityUtils.currentRole().filter(role -> role == UserRole.OPS_ADMIN).isPresent();

        Balance latestBalance = balanceRepository.findFirstByAccountIdOrderByTradingDateDesc(accountId);
        LocalDate cashSnapshotDate = latestBalance == null ? LocalDate.now() : latestBalance.getTradingDate();

        List<PortfolioOverviewResponse.CashBalanceItem> cashBalances = balanceRepository.findByAccountIdAndTradingDate(accountId, cashSnapshotDate)
                .stream()
                .sorted(Comparator.comparing(Balance::getCurrency))
                .map(item -> new PortfolioOverviewResponse.CashBalanceItem(item.getCurrency(), item.getAmount()))
                .toList();

        List<StockPosition> positions = stockPositionRepository.findByAccountIdOrderByTotalCostDescIdAsc(accountId);
        List<PortfolioOverviewResponse.HoldingItem> holdings = positions.stream()
                .map(item -> new PortfolioOverviewResponse.HoldingItem(
                        item.getSymbol(),
                        item.getMarket(),
                        item.getCurrency(),
                        item.getQuantity(),
                        item.getAveragePrice(),
                        item.getTotalCost(),
                        item.getLastTradeDate()
                ))
                .toList();

        List<PortfolioOverviewResponse.StockCostSummaryItem> stockCostByCurrency = positions.stream()
                .collect(Collectors.groupingBy(StockPosition::getCurrency,
                        Collectors.reducing(BigDecimal.ZERO, StockPosition::getTotalCost, BigDecimal::add)))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new PortfolioOverviewResponse.StockCostSummaryItem(entry.getKey(), entry.getValue()))
                .toList();

        List<StockPurchase> purchases = stockPurchaseRepository.findTop10ByAccountIdOrderByTradeDateDescIdDesc(accountId);
        List<PortfolioOverviewResponse.RecentPurchaseItem> recentPurchases = purchases.stream()
                .map(item -> new PortfolioOverviewResponse.RecentPurchaseItem(
                        item.getId(),
                        item.getSymbol(),
                        item.getMarket(),
                        item.getCurrency(),
                        item.getTradeDate(),
                        item.getSettlementDate(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getFeeAmount(),
                        item.getNetAmount()
                ))
                .toList();

        return new PortfolioOverviewResponse(
                account.getId(),
                canUnmask ? account.getAccountNo() : mask(account.getAccountNo()),
                account.getBroker(),
                account.getStatus(),
                account.getOwnerName(),
                cashSnapshotDate,
                holdings.size(),
                recentPurchases.size(),
                cashBalances,
                stockCostByCurrency,
                holdings,
                recentPurchases
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
