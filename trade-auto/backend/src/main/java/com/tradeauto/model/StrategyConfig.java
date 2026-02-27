package com.tradeauto.model;

import jakarta.persistence.*;

@Entity
@Table(name = "strategy_config")
public class StrategyConfig {
    @Id
    private Long id = 1L;

    @Column(nullable = false)
    private double riskReward = 2.0;

    @Column(nullable = false)
    private double stopLossPct = 0.07;

    @Column(nullable = false)
    private long minAvgVolume = 500000;

    @Column(nullable = false)
    private double minPrice = 5.0;

    @Column(nullable = false)
    private int lookbackDays = 252;

    @Column(nullable = false)
    private int momentumDays = 20;

    @Column(nullable = false)
    private int trendDays = 60;

    @Column(nullable = false)
    private int rsiDays = 14;

    @Column(nullable = false)
    private int volatilityDays = 20;

    @Column(nullable = false)
    private double volumeSpikeMultiplier = 1.5;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getRiskReward() {
        return riskReward;
    }

    public void setRiskReward(double riskReward) {
        this.riskReward = riskReward;
    }

    public double getStopLossPct() {
        return stopLossPct;
    }

    public void setStopLossPct(double stopLossPct) {
        this.stopLossPct = stopLossPct;
    }

    public long getMinAvgVolume() {
        return minAvgVolume;
    }

    public void setMinAvgVolume(long minAvgVolume) {
        this.minAvgVolume = minAvgVolume;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }

    public int getLookbackDays() {
        return lookbackDays;
    }

    public void setLookbackDays(int lookbackDays) {
        this.lookbackDays = lookbackDays;
    }

    public int getMomentumDays() {
        return momentumDays;
    }

    public void setMomentumDays(int momentumDays) {
        this.momentumDays = momentumDays;
    }

    public int getTrendDays() {
        return trendDays;
    }

    public void setTrendDays(int trendDays) {
        this.trendDays = trendDays;
    }

    public int getRsiDays() {
        return rsiDays;
    }

    public void setRsiDays(int rsiDays) {
        this.rsiDays = rsiDays;
    }

    public int getVolatilityDays() {
        return volatilityDays;
    }

    public void setVolatilityDays(int volatilityDays) {
        this.volatilityDays = volatilityDays;
    }

    public double getVolumeSpikeMultiplier() {
        return volumeSpikeMultiplier;
    }

    public void setVolumeSpikeMultiplier(double volumeSpikeMultiplier) {
        this.volumeSpikeMultiplier = volumeSpikeMultiplier;
    }
}
