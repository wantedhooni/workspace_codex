package com.tradingmacro.performance;

import com.tradingmacro.portfolio.PortfolioRepository;
import com.tradingmacro.strategy.StrategyRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/performance-summaries")
public class PerformanceSummaryController {
    private final PerformanceSummaryRepository repository;
    private final PortfolioRepository portfolioRepository;
    private final StrategyRepository strategyRepository;

    public PerformanceSummaryController(PerformanceSummaryRepository repository,
                                        PortfolioRepository portfolioRepository,
                                        StrategyRepository strategyRepository) {
        this.repository = repository;
        this.portfolioRepository = portfolioRepository;
        this.strategyRepository = strategyRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<PerformanceSummary> list() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public PerformanceSummary get(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Performance summary not found"));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public PerformanceSummary create(@Valid @RequestBody PerformanceSummaryRequest request) {
        PerformanceSummary summary = new PerformanceSummary();
        applyRequest(summary, request);
        return repository.save(summary);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public PerformanceSummary update(@PathVariable Long id, @Valid @RequestBody PerformanceSummaryRequest request) {
        PerformanceSummary summary = get(id);
        applyRequest(summary, request);
        return repository.save(summary);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private void applyRequest(PerformanceSummary summary, PerformanceSummaryRequest request) {
        summary.setPeriod(request.period());
        summary.setPeriodStart(request.periodStart());
        summary.setPeriodEnd(request.periodEnd());
        summary.setReturnPct(request.returnPct());
        summary.setBenchmarkReturnPct(request.benchmarkReturnPct());
        summary.setExcessReturnPct(request.excessReturnPct());
        summary.setMaxDrawdownPct(request.maxDrawdownPct());
        summary.setWinRatePct(request.winRatePct());
        summary.setProfitFactor(request.profitFactor());
        summary.setBenchmarkName(request.benchmarkName());
        if (request.portfolioId() != null) {
            summary.setPortfolio(portfolioRepository.findById(request.portfolioId())
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found")));
        } else {
            summary.setPortfolio(null);
        }
        if (request.strategyId() != null) {
            summary.setStrategy(strategyRepository.findById(request.strategyId())
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found")));
        } else {
            summary.setStrategy(null);
        }
    }
}
