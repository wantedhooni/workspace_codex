package com.tradeauto.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public class BacktestRunDTO {
    public Long id;
    public String strategyName;
    public LocalDate startDate;
    public LocalDate endDate;
    public double totalReturnPct;
    public double maxDrawdownPct;
    public double winRatePct;
    public int trades;
    public OffsetDateTime createdAt;
    public List<BacktestTradeDTO> tradeList;
}
