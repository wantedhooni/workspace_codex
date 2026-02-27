package com.quant.mvp.pipeline.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class PortfolioCatalogService {

    private final Map<Long, PortfolioInfo> portfolioStore = new LinkedHashMap<>();
    private final Map<String, Long> codeToIdStore = new LinkedHashMap<>();

    public PortfolioCatalogService() {
        seed();
    }

    public List<PortfolioInfo> search(String keyword, Boolean activeOnly) {
        String normalizedKeyword = keyword == null ? null : keyword.trim().toLowerCase(Locale.ROOT);
        boolean requireActive = Boolean.TRUE.equals(activeOnly);

        return portfolioStore.values().stream()
                .filter(row -> !requireActive || Boolean.TRUE.equals(row.active()))
                .filter(row -> normalizedKeyword == null || normalizedKeyword.isBlank() || containsKeyword(row, normalizedKeyword))
                .toList();
    }

    public PortfolioInfo requireById(Long portfolioId) {
        if (portfolioId == null || portfolioId <= 0) {
            throw new IllegalArgumentException("portfolioId must be positive");
        }

        PortfolioInfo row = portfolioStore.get(portfolioId);
        if (row == null) {
            throw new IllegalArgumentException("portfolio not found: " + portfolioId);
        }
        return row;
    }

    public Long resolvePortfolioIdByCode(String portfolioCode) {
        if (portfolioCode == null || portfolioCode.isBlank()) {
            throw new IllegalArgumentException("portfolioCode is required");
        }

        Long portfolioId = codeToIdStore.get(portfolioCode.trim().toUpperCase(Locale.ROOT));
        if (portfolioId == null) {
            throw new IllegalArgumentException("portfolio code not found: " + portfolioCode);
        }
        return portfolioId;
    }

    private boolean containsKeyword(PortfolioInfo row, String keyword) {
        return row.portfolioCode().toLowerCase(Locale.ROOT).contains(keyword)
                || row.portfolioName().toLowerCase(Locale.ROOT).contains(keyword)
                || row.strategyTag().toLowerCase(Locale.ROOT).contains(keyword)
                || row.benchmark().toLowerCase(Locale.ROOT).contains(keyword);
    }

    private void seed() {
        register(new PortfolioInfo(1L, "US_ALPHA_CORE", "US Alpha Core", "L/S Momentum", "S&P 500", "USD", true));
        register(new PortfolioInfo(2L, "US_DEF_QUALITY", "US Defensive Quality", "Quality + LowVol", "NASDAQ-100", "USD", true));
        register(new PortfolioInfo(3L, "US_VOL_HEDGE", "US Volatility Hedge", "Tail Risk Hedge", "CBOE VIX", "USD", true));
    }

    private void register(PortfolioInfo row) {
        portfolioStore.put(row.portfolioId(), row);
        codeToIdStore.put(row.portfolioCode().toUpperCase(Locale.ROOT), row.portfolioId());
    }

    public record PortfolioInfo(
            Long portfolioId,
            String portfolioCode,
            String portfolioName,
            String strategyTag,
            String benchmark,
            String baseCurrency,
            Boolean active
    ) {
        public PortfolioInfo {
            Objects.requireNonNull(portfolioId, "portfolioId");
            Objects.requireNonNull(portfolioCode, "portfolioCode");
            Objects.requireNonNull(portfolioName, "portfolioName");
            Objects.requireNonNull(strategyTag, "strategyTag");
            Objects.requireNonNull(benchmark, "benchmark");
            Objects.requireNonNull(baseCurrency, "baseCurrency");
            Objects.requireNonNull(active, "active");
        }
    }
}
