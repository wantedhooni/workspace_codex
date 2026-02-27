package com.tradingmacro.performance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tradingmacro.portfolio.Portfolio;
import com.tradingmacro.strategy.Strategy;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "performance_summaries")
public class PerformanceSummary {
    public enum Period { DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Period period;

    @NotNull
    @Column(nullable = false)
    private LocalDate periodStart;

    @NotNull
    @Column(nullable = false)
    private LocalDate periodEnd;

    @NotNull
    @Column(nullable = false)
    private BigDecimal returnPct;

    @NotNull
    @Column(nullable = false)
    private BigDecimal benchmarkReturnPct;

    @NotNull
    @Column(nullable = false)
    private BigDecimal excessReturnPct;

    @NotNull
    @Column(nullable = false)
    private BigDecimal maxDrawdownPct;

    @NotNull
    @Column(nullable = false)
    private BigDecimal winRatePct;

    @NotNull
    @Column(nullable = false)
    private BigDecimal profitFactor;

    private String benchmarkName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id")
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Strategy strategy;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Period getPeriod() { return period; }
    public void setPeriod(Period period) { this.period = period; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public BigDecimal getReturnPct() { return returnPct; }
    public void setReturnPct(BigDecimal returnPct) { this.returnPct = returnPct; }
    public BigDecimal getBenchmarkReturnPct() { return benchmarkReturnPct; }
    public void setBenchmarkReturnPct(BigDecimal benchmarkReturnPct) { this.benchmarkReturnPct = benchmarkReturnPct; }
    public BigDecimal getExcessReturnPct() { return excessReturnPct; }
    public void setExcessReturnPct(BigDecimal excessReturnPct) { this.excessReturnPct = excessReturnPct; }
    public BigDecimal getMaxDrawdownPct() { return maxDrawdownPct; }
    public void setMaxDrawdownPct(BigDecimal maxDrawdownPct) { this.maxDrawdownPct = maxDrawdownPct; }
    public BigDecimal getWinRatePct() { return winRatePct; }
    public void setWinRatePct(BigDecimal winRatePct) { this.winRatePct = winRatePct; }
    public BigDecimal getProfitFactor() { return profitFactor; }
    public void setProfitFactor(BigDecimal profitFactor) { this.profitFactor = profitFactor; }
    public String getBenchmarkName() { return benchmarkName; }
    public void setBenchmarkName(String benchmarkName) { this.benchmarkName = benchmarkName; }
    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }
    public Strategy getStrategy() { return strategy; }
    public void setStrategy(Strategy strategy) { this.strategy = strategy; }
    public Instant getCreatedAt() { return createdAt; }
}
