package com.quant.mvp.pipeline.service;

import com.quant.mvp.pipeline.domain.Order;
import com.quant.mvp.pipeline.domain.OrderStatus;
import com.quant.mvp.pipeline.domain.PortfolioSummary;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class OrderWorkbenchService {

    private static final BigDecimal DEFAULT_MARKET_PRICE = new BigDecimal("100");
    private static final int DEFAULT_STALE_MINUTES = 30;
    private static final int DEFAULT_TOP_SYMBOLS = 8;

    private final OrderTradePositionPipelineService pipelineService;

    public OrderWorkbenchService(OrderTradePositionPipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    public OrderWorkbenchSnapshot snapshot(Long portfolioId, Integer staleMinutes, Integer topN) {
        int normalizedStaleMinutes = staleMinutes == null ? DEFAULT_STALE_MINUTES : staleMinutes;
        if (normalizedStaleMinutes < 0) {
            throw new IllegalArgumentException("staleMinutes must be >= 0");
        }

        int normalizedTopN = topN == null ? DEFAULT_TOP_SYMBOLS : topN;
        if (normalizedTopN <= 0) {
            throw new IllegalArgumentException("topN must be > 0");
        }

        List<Order> orders = pipelineService.searchOrders(portfolioId, null, null);
        Instant now = Instant.now();

        Map<OrderStatus, StatusCounterRow> statusMap = new EnumMap<>(OrderStatus.class);
        Map<String, SymbolAggregate> symbolMap = new HashMap<>();
        List<StaleOrderRow> staleOrderRows = new ArrayList<>();

        long openOrderCount = 0;
        BigDecimal openNotional = BigDecimal.ZERO;

        for (Order order : orders) {
            BigDecimal requestedNotional = estimateNotional(order.quantity(), order.limitPrice());
            statusMap.merge(
                    order.status(),
                    new StatusCounterRow(order.status().name(), 1L, requestedNotional),
                    (left, right) -> new StatusCounterRow(
                            left.status(),
                            left.count() + right.count(),
                            scale6(left.estimatedNotional().add(right.estimatedNotional()))
                    )
            );

            SymbolAggregate symbolAgg = symbolMap.computeIfAbsent(order.symbol(), key -> new SymbolAggregate());
            symbolAgg.requestedQuantity = symbolAgg.requestedQuantity.add(order.quantity());
            symbolAgg.filledQuantity = symbolAgg.filledQuantity.add(order.filledQuantity());

            if (isOpenOrder(order.status())) {
                openOrderCount += 1;
                BigDecimal remainingQuantity = remainingQuantity(order);
                BigDecimal remainingNotional = estimateNotional(remainingQuantity, order.limitPrice());
                openNotional = openNotional.add(remainingNotional);
                symbolAgg.openOrderCount += 1;
                symbolAgg.openNotional = symbolAgg.openNotional.add(remainingNotional);

                long ageMinutes = Duration.between(order.createdAt(), now).toMinutes();
                if (ageMinutes >= normalizedStaleMinutes) {
                    staleOrderRows.add(new StaleOrderRow(
                            order.orderId(),
                            order.symbol(),
                            order.status().name(),
                            scale6(remainingQuantity),
                            ageMinutes,
                            order.createdAt()
                    ));
                }
            }
        }

        List<StatusCounterRow> statusCounters = statusMap.values().stream()
                .sorted(Comparator.comparing(StatusCounterRow::status))
                .toList();

        List<TopSymbolRow> topSymbols = symbolMap.entrySet().stream()
                .map(entry -> toTopSymbolRow(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(TopSymbolRow::openNotional).reversed()
                        .thenComparing(TopSymbolRow::openOrderCount).reversed()
                        .thenComparing(TopSymbolRow::symbol))
                .limit(normalizedTopN)
                .toList();

        List<StaleOrderRow> staleOrders = staleOrderRows.stream()
                .sorted(Comparator.comparing(StaleOrderRow::orderAgeMinutes).reversed()
                        .thenComparing(StaleOrderRow::orderId))
                .limit(10)
                .toList();

        PortfolioSummaryAccumulator summaryAccumulator = toSummaryAccumulator(portfolioId);
        SummaryRow summary = new SummaryRow(
                portfolioId,
                normalizedStaleMinutes,
                (long) orders.size(),
                openOrderCount,
                (long) staleOrderRows.size(),
                scale6(openNotional),
                scale6(summaryAccumulator.dailyTurnover),
                scale6(summaryAccumulator.turnoverUsagePct),
                scale6(summaryAccumulator.totalPnl),
                now
        );

        return new OrderWorkbenchSnapshot(summary, statusCounters, topSymbols, staleOrders);
    }

    private PortfolioSummaryAccumulator toSummaryAccumulator(Long portfolioId) {
        List<PortfolioSummary> rows = pipelineService.searchPortfolioSummaries(portfolioId);
        if (rows.isEmpty()) {
            return new PortfolioSummaryAccumulator(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        if (portfolioId != null) {
            PortfolioSummary row = rows.get(0);
            return new PortfolioSummaryAccumulator(
                    nvl(row.dailyTurnover()),
                    nvl(row.turnoverUsagePct()),
                    nvl(row.totalPnl())
            );
        }

        BigDecimal dailyTurnover = BigDecimal.ZERO;
        BigDecimal turnoverUsagePct = BigDecimal.ZERO;
        BigDecimal totalPnl = BigDecimal.ZERO;

        for (PortfolioSummary row : rows) {
            dailyTurnover = dailyTurnover.add(nvl(row.dailyTurnover()));
            turnoverUsagePct = turnoverUsagePct.add(nvl(row.turnoverUsagePct()));
            totalPnl = totalPnl.add(nvl(row.totalPnl()));
        }

        BigDecimal avgTurnoverUsagePct = turnoverUsagePct.divide(
                BigDecimal.valueOf(rows.size()),
                6,
                RoundingMode.HALF_UP
        );

        return new PortfolioSummaryAccumulator(dailyTurnover, avgTurnoverUsagePct, totalPnl);
    }

    private TopSymbolRow toTopSymbolRow(String symbol, SymbolAggregate agg) {
        BigDecimal fillRatePct = agg.requestedQuantity.compareTo(BigDecimal.ZERO) <= 0
                ? BigDecimal.ZERO
                : agg.filledQuantity.divide(agg.requestedQuantity, 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        return new TopSymbolRow(
                symbol,
                agg.openOrderCount,
                scale6(agg.openNotional),
                scale6(agg.requestedQuantity),
                scale6(agg.filledQuantity),
                scale6(fillRatePct)
        );
    }

    private boolean isOpenOrder(OrderStatus status) {
        return status == OrderStatus.NEW || status == OrderStatus.SENT || status == OrderStatus.PARTIAL;
    }

    private BigDecimal remainingQuantity(Order order) {
        BigDecimal remaining = nvl(order.quantity()).subtract(nvl(order.filledQuantity()));
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return remaining;
    }

    private BigDecimal estimateNotional(BigDecimal quantity, BigDecimal price) {
        BigDecimal normalizedQty = nvl(quantity);
        BigDecimal normalizedPrice = nvl(price);
        if (normalizedPrice.compareTo(BigDecimal.ZERO) <= 0) {
            normalizedPrice = DEFAULT_MARKET_PRICE;
        }
        return scale6(normalizedQty.multiply(normalizedPrice));
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal scale6(BigDecimal value) {
        return nvl(value).setScale(6, RoundingMode.HALF_UP);
    }

    private static final class SymbolAggregate {
        private long openOrderCount;
        private BigDecimal openNotional = BigDecimal.ZERO;
        private BigDecimal requestedQuantity = BigDecimal.ZERO;
        private BigDecimal filledQuantity = BigDecimal.ZERO;
    }

    private record PortfolioSummaryAccumulator(
            BigDecimal dailyTurnover,
            BigDecimal turnoverUsagePct,
            BigDecimal totalPnl
    ) {
        private PortfolioSummaryAccumulator {
            Objects.requireNonNull(dailyTurnover, "dailyTurnover");
            Objects.requireNonNull(turnoverUsagePct, "turnoverUsagePct");
            Objects.requireNonNull(totalPnl, "totalPnl");
        }
    }

    public record SummaryRow(
            Long portfolioId,
            Integer staleMinutes,
            Long totalOrderCount,
            Long openOrderCount,
            Long staleOrderCount,
            BigDecimal openNotional,
            BigDecimal dailyTurnover,
            BigDecimal turnoverUsagePct,
            BigDecimal totalPnl,
            Instant generatedAt
    ) {
    }

    public record StatusCounterRow(
            String status,
            Long count,
            BigDecimal estimatedNotional
    ) {
    }

    public record TopSymbolRow(
            String symbol,
            Long openOrderCount,
            BigDecimal openNotional,
            BigDecimal requestedQuantity,
            BigDecimal filledQuantity,
            BigDecimal fillRatePct
    ) {
    }

    public record StaleOrderRow(
            Long orderId,
            String symbol,
            String status,
            BigDecimal remainingQuantity,
            Long orderAgeMinutes,
            Instant createdAt
    ) {
    }

    public record OrderWorkbenchSnapshot(
            SummaryRow summary,
            List<StatusCounterRow> statusCounters,
            List<TopSymbolRow> topSymbols,
            List<StaleOrderRow> staleOrders
    ) {
    }
}
