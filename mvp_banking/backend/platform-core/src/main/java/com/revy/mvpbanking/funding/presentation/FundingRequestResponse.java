package com.revy.mvpbanking.funding.presentation;

import com.revy.mvpbanking.funding.domain.FundingRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FundingRequestResponse(
        UUID id,
        UUID customerId,
        String customerEmail,
        UUID accountId,
        String accountNumber,
        String accountType,
        String requestNumber,
        String requestType,
        String status,
        BigDecimal amount,
        String currency,
        BigDecimal balanceSnapshot,
        String note,
        String settlementTransactionNumber,
        Instant settledAt,
        Instant createdAt
) {
    public static FundingRequestResponse from(FundingRequest request) {
        return new FundingRequestResponse(
                request.getId(),
                request.getCustomerId(),
                request.getCustomerEmail(),
                request.getAccountId(),
                request.getAccountNumber(),
                request.getAccountType(),
                request.getRequestNumber(),
                request.getRequestType().name(),
                request.getStatus().name(),
                request.getAmount(),
                request.getCurrency(),
                request.getBalanceSnapshot(),
                request.getNote(),
                request.getSettlementTransactionNumber(),
                request.getSettledAt(),
                request.getCreatedAt()
        );
    }
}
