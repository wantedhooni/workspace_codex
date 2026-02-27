package com.derivops.mvp.integration;

import java.math.BigDecimal;

public record MarginSnapshot(
        BigDecimal initialMargin,
        BigDecimal maintenanceMargin,
        BigDecimal availableMargin
) {
}
