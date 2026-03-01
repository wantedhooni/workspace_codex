package com.derivops.mvp.approval.dto;
import com.derivops.mvp.approval.*;
import com.derivops.mvp.approval.api.*;
import com.derivops.mvp.approval.application.*;
import com.derivops.mvp.approval.infrastructure.*;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpsertApprovalPolicyRequest(
        @NotBlank String brokerCode,
        @NotNull ApprovalDomain domain,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal highThreshold,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal urgentThreshold,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal manualReviewThreshold,
        boolean sameDayAutoReview,
        boolean enabled,
        @NotNull LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String description
) {
}
