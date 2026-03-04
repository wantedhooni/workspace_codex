package com.example.samplesecurebff.downstream;

import java.math.BigDecimal;

public record PositionView(
        String instrumentCode,
        long quantity,
        BigDecimal marketValue
) {
}
