package com.example.securities.eod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EodDtos {

    public record RunEodRequest(LocalDate businessDate) {
    }

    public record EodSnapshotResponse(
            String id,
            LocalDate businessDate,
            Long accountCount,
            BigDecimal totalBalance,
            String reconciliationStatus,
            LocalDateTime createdAt
    ) {
    }
}
