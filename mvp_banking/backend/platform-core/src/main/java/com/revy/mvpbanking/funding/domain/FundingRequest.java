package com.revy.mvpbanking.funding.domain;

import com.revy.mvpbanking.common.domain.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "funding_requests")
public class FundingRequest extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "customer_email", nullable = false, length = 120)
    private String customerEmail;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "account_number", nullable = false, length = 40)
    private String accountNumber;

    @Column(name = "account_type", nullable = false, length = 30)
    private String accountType;

    @Column(name = "request_number", nullable = false, unique = true, length = 50)
    private String requestNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 20)
    private FundingRequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FundingRequestStatus status;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "balance_snapshot", nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceSnapshot;

    @Column(name = "service_fee_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal serviceFeeAmount;

    @Column(name = "priority_processing", nullable = false)
    private boolean priorityProcessing;

    @Column(name = "priority_fee_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal priorityFeeAmount;

    @Column(name = "total_debit_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalDebitAmount;

    @Column(name = "linked_bank_account_id")
    private UUID linkedBankAccountId;

    @Column(name = "linked_bank_name", length = 80)
    private String linkedBankName;

    @Column(name = "linked_bank_account_alias", length = 80)
    private String linkedBankAccountAlias;

    @Column(name = "linked_bank_account_number_masked", length = 20)
    private String linkedBankAccountNumberMasked;

    @Column(name = "linked_bank_account_holder_name", length = 120)
    private String linkedBankAccountHolderName;

    @Column(name = "daily_limit_amount", precision = 19, scale = 4)
    private BigDecimal dailyLimitAmount;

    @Column(name = "daily_accumulated_amount", precision = 19, scale = 4)
    private BigDecimal dailyAccumulatedAmount;

    @Column(name = "daily_limit_exceeded", nullable = false)
    private boolean dailyLimitExceeded;

    @Column(name = "same_day_settlement_eligible", nullable = false)
    private boolean sameDaySettlementEligible;

    @Column(name = "expected_settlement_at")
    private Instant expectedSettlementAt;

    @Column(name = "manual_review_required", nullable = false)
    private boolean manualReviewRequired;

    @Column(name = "manual_review_reason", length = 255)
    private String manualReviewReason;

    @Column(length = 255)
    private String note;

    @Column(name = "settlement_transaction_number", length = 50)
    private String settlementTransactionNumber;

    @Column(name = "settled_at")
    private Instant settledAt;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;

    @Column(name = "canceled_at")
    private Instant canceledAt;

    protected FundingRequest() {
    }

    public FundingRequest(
            UUID customerId,
            String customerEmail,
            UUID accountId,
            String accountNumber,
            String accountType,
            String requestNumber,
            FundingRequestType requestType,
            BigDecimal amount,
            String currency,
            BigDecimal balanceSnapshot,
            BigDecimal serviceFeeAmount,
            boolean priorityProcessing,
            BigDecimal priorityFeeAmount,
            BigDecimal totalDebitAmount,
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
            String note
    ) {
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.requestNumber = requestNumber;
        this.requestType = requestType;
        this.status = FundingRequestStatus.PENDING_APPROVAL;
        this.amount = amount;
        this.currency = currency;
        this.balanceSnapshot = balanceSnapshot;
        this.serviceFeeAmount = serviceFeeAmount;
        this.priorityProcessing = priorityProcessing;
        this.priorityFeeAmount = priorityFeeAmount;
        this.totalDebitAmount = totalDebitAmount;
        this.linkedBankAccountId = linkedBankAccountId;
        this.linkedBankName = linkedBankName;
        this.linkedBankAccountAlias = linkedBankAccountAlias;
        this.linkedBankAccountNumberMasked = linkedBankAccountNumberMasked;
        this.linkedBankAccountHolderName = linkedBankAccountHolderName;
        this.dailyLimitAmount = dailyLimitAmount;
        this.dailyAccumulatedAmount = dailyAccumulatedAmount;
        this.dailyLimitExceeded = dailyLimitExceeded;
        this.sameDaySettlementEligible = sameDaySettlementEligible;
        this.expectedSettlementAt = expectedSettlementAt;
        this.manualReviewRequired = manualReviewRequired;
        this.manualReviewReason = manualReviewReason;
        this.note = note;
    }

    public void approve(String settlementTransactionNumber, Instant settledAt) {
        if (status == FundingRequestStatus.APPROVED) {
            return;
        }
        if (status == FundingRequestStatus.REJECTED) {
            throw new IllegalStateException("Rejected funding request cannot be approved");
        }
        if (status == FundingRequestStatus.CANCELED) {
            throw new IllegalStateException("Canceled funding request cannot be approved");
        }
        this.status = FundingRequestStatus.APPROVED;
        this.settlementTransactionNumber = settlementTransactionNumber;
        this.settledAt = settledAt;
        this.cancellationReason = null;
        this.canceledAt = null;
    }

    public void reject() {
        if (status == FundingRequestStatus.REJECTED) {
            return;
        }
        if (status == FundingRequestStatus.APPROVED) {
            throw new IllegalStateException("Approved funding request cannot be rejected");
        }
        if (status == FundingRequestStatus.CANCELED) {
            throw new IllegalStateException("Canceled funding request cannot be rejected");
        }
        this.status = FundingRequestStatus.REJECTED;
        this.settlementTransactionNumber = null;
        this.settledAt = null;
        this.cancellationReason = null;
        this.canceledAt = null;
    }

    public void cancel(String reason) {
        if (status == FundingRequestStatus.CANCELED) {
            return;
        }
        if (status == FundingRequestStatus.APPROVED) {
            throw new IllegalStateException("Approved funding request cannot be canceled");
        }
        if (status == FundingRequestStatus.REJECTED) {
            throw new IllegalStateException("Rejected funding request cannot be canceled");
        }
        this.status = FundingRequestStatus.CANCELED;
        this.cancellationReason = reason;
        this.canceledAt = Instant.now();
        this.settlementTransactionNumber = null;
        this.settledAt = null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getRequestNumber() {
        return requestNumber;
    }

    public FundingRequestType getRequestType() {
        return requestType;
    }

    public FundingRequestStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalanceSnapshot() {
        return balanceSnapshot;
    }

    public BigDecimal getServiceFeeAmount() {
        return serviceFeeAmount;
    }

    public boolean isPriorityProcessing() {
        return priorityProcessing;
    }

    public BigDecimal getPriorityFeeAmount() {
        return priorityFeeAmount;
    }

    public BigDecimal getTotalDebitAmount() {
        return totalDebitAmount;
    }

    public UUID getLinkedBankAccountId() {
        return linkedBankAccountId;
    }

    public String getLinkedBankName() {
        return linkedBankName;
    }

    public String getLinkedBankAccountAlias() {
        return linkedBankAccountAlias;
    }

    public String getLinkedBankAccountNumberMasked() {
        return linkedBankAccountNumberMasked;
    }

    public String getLinkedBankAccountHolderName() {
        return linkedBankAccountHolderName;
    }

    public BigDecimal getDailyLimitAmount() {
        return dailyLimitAmount;
    }

    public BigDecimal getDailyAccumulatedAmount() {
        return dailyAccumulatedAmount;
    }

    public boolean isDailyLimitExceeded() {
        return dailyLimitExceeded;
    }

    public boolean isSameDaySettlementEligible() {
        return sameDaySettlementEligible;
    }

    public Instant getExpectedSettlementAt() {
        return expectedSettlementAt;
    }

    public boolean isManualReviewRequired() {
        return manualReviewRequired;
    }

    public String getManualReviewReason() {
        return manualReviewReason;
    }

    public String getNote() {
        return note;
    }

    public String getSettlementTransactionNumber() {
        return settlementTransactionNumber;
    }

    public Instant getSettledAt() {
        return settledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public Instant getCanceledAt() {
        return canceledAt;
    }
}
