package com.tradeauto.dto;

import com.tradeauto.model.SignalAction;
import java.time.LocalDate;

public class SignalDTO {
    public Long id;
    public String symbol;
    public String name;
    public LocalDate signalDate;
    public SignalAction action;
    public double score;
    public double entryPrice;
    public double stopLoss;
    public double takeProfit;
    public String rationale;
}
