package com.tradeauto.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "signals", indexes = {
        @Index(name = "idx_signal_ticker_date", columnList = "ticker_id,signal_date")
})
public class Signal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ticker_id", nullable = false)
    private Ticker ticker;

    @Column(name = "signal_date", nullable = false)
    private LocalDate signalDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SignalAction action;

    @Column(nullable = false)
    private double score;

    @Column(name = "entry_price", nullable = false)
    private double entryPrice;

    @Column(name = "stop_loss", nullable = false)
    private double stopLoss;

    @Column(name = "take_profit", nullable = false)
    private double takeProfit;

    @Column(length = 1000)
    private String rationale;

    public Long getId() {
        return id;
    }

    public Ticker getTicker() {
        return ticker;
    }

    public void setTicker(Ticker ticker) {
        this.ticker = ticker;
    }

    public LocalDate getSignalDate() {
        return signalDate;
    }

    public void setSignalDate(LocalDate signalDate) {
        this.signalDate = signalDate;
    }

    public SignalAction getAction() {
        return action;
    }

    public void setAction(SignalAction action) {
        this.action = action;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public double getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(double stopLoss) {
        this.stopLoss = stopLoss;
    }

    public double getTakeProfit() {
        return takeProfit;
    }

    public void setTakeProfit(double takeProfit) {
        this.takeProfit = takeProfit;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }
}
