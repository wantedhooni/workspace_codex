package com.derivops.mvp.approval.dto;
import com.derivops.mvp.approval.*;
import com.derivops.mvp.approval.api.*;
import com.derivops.mvp.approval.application.*;
import com.derivops.mvp.approval.infrastructure.*;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ApprovalPolicyResponse(
        Long id,
        String brokerCode,
        ApprovalDomain domain,
        BigDecimal highThreshold,
        BigDecimal urgentThreshold,
        BigDecimal manualReviewThreshold,
        boolean sameDayAutoReview,
        boolean enabled,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String description,
        OffsetDateTime createdAt
) {
}
