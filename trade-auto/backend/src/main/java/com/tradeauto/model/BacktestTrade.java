package com.tradeauto.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "backtest_trades")
public class BacktestTrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private BacktestRun run;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ticker_id", nullable = false)
    private Ticker ticker;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "exit_date", nullable = false)
    private LocalDate exitDate;

    @Column(name = "entry_price", nullable = false)
    private double entryPrice;

    @Column(name = "exit_price", nullable = false)
    private double exitPrice;

    @Column(name = "pnl_pct", nullable = false)
    private double pnlPct;

    @Column(name = "exit_reason", nullable = false, length = 50)
    private String exitReason;

    public Long getId() {
        return id;
    }

    public BacktestRun getRun() {
        return run;
    }

    public void setRun(BacktestRun run) {
        this.run = run;
    }

    public Ticker getTicker() {
        return ticker;
    }

    public void setTicker(Ticker ticker) {
        this.ticker = ticker;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public LocalDate getExitDate() {
        return exitDate;
    }

    public void setExitDate(LocalDate exitDate) {
        this.exitDate = exitDate;
    }

    public double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public double getExitPrice() {
        return exitPrice;
    }

    public void setExitPrice(double exitPrice) {
        this.exitPrice = exitPrice;
    }

    public double getPnlPct() {
        return pnlPct;
    }

    public void setPnlPct(double pnlPct) {
        this.pnlPct = pnlPct;
    }

    public String getExitReason() {
        return exitReason;
    }

    public void setExitReason(String exitReason) {
        this.exitReason = exitReason;
    }
}
