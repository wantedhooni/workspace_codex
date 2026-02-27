package com.tradeauto.dto;

import java.time.LocalDate;

public class RunBacktestRequest {
    public LocalDate startDate;
    public LocalDate endDate;
    public String strategyName;
}
