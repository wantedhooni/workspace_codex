package com.derivops.mvp.stockrecommendation.application;

import com.derivops.mvp.audit.application.AuditLogService;
import com.derivops.mvp.common.SecurityUtils;
import com.derivops.mvp.common.ServiceUnavailableException;
import com.derivops.mvp.portfolio.application.PortfolioService;
import com.derivops.mvp.portfolio.dto.PortfolioOverviewResponse;
import com.derivops.mvp.stockrecommendation.dto.GenerateStockRecommendationRequest;
import com.derivops.mvp.stockrecommendation.dto.StockRecommendationResponse;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StockRecommendationService {

    private final PortfolioService portfolioService;
    private final StockRecommendationGenerator stockRecommendationGenerator;
    private final StockRecommendationProperties properties;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public StockRecommendationResponse recommend(GenerateStockRecommendationRequest request) {
        if (!properties.enabled()) {
            throw new ServiceUnavailableException("AI stock recommendation is disabled");
        }

        int maxRecommendations = request.maxRecommendations() == null
                ? Math.min(3, properties.maxRecommendations())
                : Math.min(request.maxRecommendations(), properties.maxRecommendations());

        PortfolioOverviewResponse overview = portfolioService.getOverview(request.accountId(), false);
        StockRecommendationPromptContext context = new StockRecommendationPromptContext(
                overview.accountId(),
                overview.accountNo(),
                overview.broker(),
                overview.status(),
                overview.ownerName(),
                overview.cashSnapshotDate(),
                request.riskProfile(),
                request.investmentHorizon(),
                maxRecommendations,
                normalize(request.preferredMarkets()),
                normalize(request.candidateSymbols()),
                request.operatorView(),
                overview.cashBalances().stream()
                        .map(item -> new StockRecommendationPromptContext.CashBalanceItem(item.currency(), item.amount()))
                        .toList(),
                overview.holdings().stream()
                        .map(item -> new StockRecommendationPromptContext.HoldingItem(
                                item.symbol(),
                                item.market(),
                                item.currency(),
                                item.quantity(),
                                item.averagePrice(),
                                item.totalCost(),
                                item.lastTradeDate()
                        ))
                        .toList(),
                overview.recentPurchases().stream()
                        .map(item -> new StockRecommendationPromptContext.RecentPurchaseItem(
                                item.symbol(),
                                item.market(),
                                item.currency(),
                                item.tradeDate(),
                                item.quantity(),
                                item.price(),
                                item.netAmount()
                        ))
                        .toList()
        );

        StockRecommendationGenerator.GeneratedRecommendation generated = stockRecommendationGenerator.generate(context);
        auditLogService.log(
                SecurityUtils.currentUsername(),
                "GENERATE_STOCK_RECOMMENDATION",
                "ACCOUNT",
                String.valueOf(request.accountId()),
                "provider=%s model=%s risk=%s horizon=%s max=%d".formatted(
                        properties.provider(),
                        properties.modelName(),
                        request.riskProfile(),
                        request.investmentHorizon(),
                        maxRecommendations
                )
        );

        return new StockRecommendationResponse(
                overview.accountId(),
                overview.accountNo(),
                properties.provider(),
                properties.modelName(),
                OffsetDateTime.now(),
                generated.summary(),
                generated.cautionPoints(),
                generated.recommendations().stream()
                        .map(item -> new StockRecommendationResponse.RecommendationItem(
                                item.rank(),
                                item.symbol(),
                                item.market(),
                                item.action(),
                                item.confidence(),
                                item.allocationHint(),
                                item.rationale(),
                                item.riskNotes()
                        ))
                        .toList(),
                new StockRecommendationResponse.PortfolioSnapshot(
                        overview.cashSnapshotDate(),
                        overview.cashBalances().stream()
                                .map(item -> new StockRecommendationResponse.CashBalanceItem(item.currency(), item.amount()))
                                .toList(),
                        overview.holdings().stream()
                                .map(item -> new StockRecommendationResponse.HoldingItem(
                                        item.symbol(),
                                        item.market(),
                                        item.currency(),
                                        item.quantity(),
                                        item.averagePrice(),
                                        item.totalCost(),
                                        item.lastTradeDate()
                                ))
                                .toList()
                ),
                properties.disclaimer()
        );
    }

    private List<String> normalize(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .distinct()
                .toList();
    }
}
