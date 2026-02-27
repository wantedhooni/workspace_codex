package com.tradingmacro.risk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tradingmacro.portfolio.Portfolio;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "risk_policies")
public class RiskPolicy {
    public enum Status { ACTIVE, REVIEW, DISABLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(nullable = false)
    private BigDecimal maxDailyLoss;

    @NotNull
    @Column(nullable = false)
    private BigDecimal maxPositionSize;

    @NotNull
    @Column(nullable = false)
    private BigDecimal maxLeverage;

    @Column(length = 1000)
    private String allowedAssetClasses;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Portfolio portfolio;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getMaxDailyLoss() { return maxDailyLoss; }
    public void setMaxDailyLoss(BigDecimal maxDailyLoss) { this.maxDailyLoss = maxDailyLoss; }
    public BigDecimal getMaxPositionSize() { return maxPositionSize; }
    public void setMaxPositionSize(BigDecimal maxPositionSize) { this.maxPositionSize = maxPositionSize; }
    public BigDecimal getMaxLeverage() { return maxLeverage; }
    public void setMaxLeverage(BigDecimal maxLeverage) { this.maxLeverage = maxLeverage; }
    public String getAllowedAssetClasses() { return allowedAssetClasses; }
    public void setAllowedAssetClasses(String allowedAssetClasses) { this.allowedAssetClasses = allowedAssetClasses; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
