package com.example.samplesecurebff.downstream;

import java.math.BigDecimal;

public record RiskSnapshot(
        String accountId,
        BigDecimal exposure,
        String riskLevel
) {
}
