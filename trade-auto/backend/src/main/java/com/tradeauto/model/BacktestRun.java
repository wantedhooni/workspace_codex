package com.tradeauto.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "backtest_runs")
public class BacktestRun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "strategy_name", nullable = false, length = 100)
    private String strategyName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_return_pct", nullable = false)
    private double totalReturnPct;

    @Column(name = "max_drawdown_pct", nullable = false)
    private double maxDrawdownPct;

    @Column(name = "win_rate_pct", nullable = false)
    private double winRatePct;

    @Column(nullable = false)
    private int trades;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BacktestTrade> tradeList = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getTotalReturnPct() {
        return totalReturnPct;
    }

    public void setTotalReturnPct(double totalReturnPct) {
        this.totalReturnPct = totalReturnPct;
    }

    public double getMaxDrawdownPct() {
        return maxDrawdownPct;
    }

    public void setMaxDrawdownPct(double maxDrawdownPct) {
        this.maxDrawdownPct = maxDrawdownPct;
    }

    public double getWinRatePct() {
        return winRatePct;
    }

    public void setWinRatePct(double winRatePct) {
        this.winRatePct = winRatePct;
    }

    public int getTrades() {
        return trades;
    }

    public void setTrades(int trades) {
        this.trades = trades;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public List<BacktestTrade> getTradeList() {
        return tradeList;
    }
}
