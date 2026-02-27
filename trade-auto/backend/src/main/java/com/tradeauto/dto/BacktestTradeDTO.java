package com.tradeauto.dto;

import java.time.LocalDate;

public class BacktestTradeDTO {
    public Long id;
    public String symbol;
    public LocalDate entryDate;
    public LocalDate exitDate;
    public double entryPrice;
    public double exitPrice;
    public double pnlPct;
    public String exitReason;
}
