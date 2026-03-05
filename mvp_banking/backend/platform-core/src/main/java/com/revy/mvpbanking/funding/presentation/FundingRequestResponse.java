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
        BigDecimal serviceFeeAmount,
        boolean priorityProcessing,
        BigDecimal priorityFeeAmount,
        BigDecimal totalDebitAmount,
        String currency,
        BigDecimal balanceSnapshot,
        UUID linkedBankAccountId,
        String linkedBankName,
        String linkedBankAccountAlias,
        String linkedBankAccountNumberMasked,
        String linkedBankAccountHolderName,
        BigDecimal dailyLimitAmount,
        BigDecimal dailyAccumulatedAmount,
        boolean dailyLimitExceeded,
        boolean sameDaySettlementEligible,
        Instant expectedSettlementAt,
        boolean manualReviewRequired,
        String manualReviewReason,
        String note,
        String cancellationReason,
        Instant canceledAt,
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
                request.getServiceFeeAmount(),
                request.isPriorityProcessing(),
                request.getPriorityFeeAmount(),
                request.getTotalDebitAmount(),
                request.getCurrency(),
                request.getBalanceSnapshot(),
                request.getLinkedBankAccountId(),
                request.getLinkedBankName(),
                request.getLinkedBankAccountAlias(),
                request.getLinkedBankAccountNumberMasked(),
                request.getLinkedBankAccountHolderName(),
                request.getDailyLimitAmount(),
                request.getDailyAccumulatedAmount(),
                request.isDailyLimitExceeded(),
                request.isSameDaySettlementEligible(),
                request.getExpectedSettlementAt(),
                request.isManualReviewRequired(),
                request.getManualReviewReason(),
                request.getNote(),
                request.getCancellationReason(),
                request.getCanceledAt(),
                request.getSettlementTransactionNumber(),
                request.getSettledAt(),
                request.getCreatedAt()
        );
    }
}
