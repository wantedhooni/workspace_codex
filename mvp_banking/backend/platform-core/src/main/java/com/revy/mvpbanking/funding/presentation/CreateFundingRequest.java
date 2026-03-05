package com.revy.mvpbanking.funding.presentation;

import com.revy.mvpbanking.funding.domain.FundingRequestType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateFundingRequest(
        @NotNull UUID accountId,
        @NotNull FundingRequestType requestType,
        @NotNull @DecimalMin("0.0001") BigDecimal amount,
        UUID linkedBankAccountId,
        Boolean priorityProcessing,
        @Size(max = 255) String note
) {
}
