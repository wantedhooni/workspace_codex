package com.derivops.mvp.risk.dto;
import com.derivops.mvp.risk.*;
import com.derivops.mvp.risk.api.*;
import com.derivops.mvp.risk.application.*;
import com.derivops.mvp.risk.infrastructure.*;


import com.derivops.mvp.approval.ApprovalDomain;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record RiskLimitPolicyResponse(
        Long id,
        String brokerCode,
        ApprovalDomain domain,
        String currencyCode,
        BigDecimal maxPerRequest,
        BigDecimal dailySoftLimit,
        BigDecimal dailyHardLimit,
        boolean enabled,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String description,
        OffsetDateTime createdAt
) {
}
