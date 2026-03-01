package com.derivops.mvp.risk.dto;
import com.derivops.mvp.risk.*;
import com.derivops.mvp.risk.api.*;
import com.derivops.mvp.risk.application.*;
import com.derivops.mvp.risk.infrastructure.*;


import com.derivops.mvp.approval.ApprovalDomain;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpsertRiskLimitPolicyRequest(
        @NotBlank String brokerCode,
        @NotNull ApprovalDomain domain,
        String currencyCode,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal maxPerRequest,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal dailySoftLimit,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal dailyHardLimit,
        boolean enabled,
        @NotNull LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String description
) {
}
