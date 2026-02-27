package com.tradingmacro.performance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tradingmacro.portfolio.Portfolio;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "performance_snapshots")
public class PerformanceSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDate snapshotDate;

    @NotNull
    @Column(nullable = false)
    private BigDecimal pnl;

    @NotNull
    @Column(nullable = false)
    private BigDecimal returnPct;

    @NotNull
    @Column(nullable = false)
    private BigDecimal drawdownPct;

    @NotNull
    @Column(nullable = false)
    private BigDecimal sharpeRatio;

    @NotNull
    @Column(nullable = false)
    private BigDecimal volatilityPct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Portfolio portfolio;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public LocalDate getSnapshotDate() { return snapshotDate; }
    public void setSnapshotDate(LocalDate snapshotDate) { this.snapshotDate = snapshotDate; }
    public BigDecimal getPnl() { return pnl; }
    public void setPnl(BigDecimal pnl) { this.pnl = pnl; }
    public BigDecimal getReturnPct() { return returnPct; }
    public void setReturnPct(BigDecimal returnPct) { this.returnPct = returnPct; }
    public BigDecimal getDrawdownPct() { return drawdownPct; }
    public void setDrawdownPct(BigDecimal drawdownPct) { this.drawdownPct = drawdownPct; }
    public BigDecimal getSharpeRatio() { return sharpeRatio; }
    public void setSharpeRatio(BigDecimal sharpeRatio) { this.sharpeRatio = sharpeRatio; }
    public BigDecimal getVolatilityPct() { return volatilityPct; }
    public void setVolatilityPct(BigDecimal volatilityPct) { this.volatilityPct = volatilityPct; }
    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }
    public Instant getCreatedAt() { return createdAt; }
}
