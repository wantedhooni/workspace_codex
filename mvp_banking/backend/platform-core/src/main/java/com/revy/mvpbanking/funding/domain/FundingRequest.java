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

    @Column(length = 255)
    private String note;

    @Column(name = "settlement_transaction_number", length = 50)
    private String settlementTransactionNumber;

    @Column(name = "settled_at")
    private Instant settledAt;

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
        this.note = note;
    }

    public void approve(String settlementTransactionNumber, Instant settledAt) {
        if (status == FundingRequestStatus.APPROVED) {
            return;
        }
        if (status == FundingRequestStatus.REJECTED) {
            throw new IllegalStateException("Rejected funding request cannot be approved");
        }
        this.status = FundingRequestStatus.APPROVED;
        this.settlementTransactionNumber = settlementTransactionNumber;
        this.settledAt = settledAt;
    }

    public void reject() {
        if (status == FundingRequestStatus.REJECTED) {
            return;
        }
        if (status == FundingRequestStatus.APPROVED) {
            throw new IllegalStateException("Approved funding request cannot be rejected");
        }
        this.status = FundingRequestStatus.REJECTED;
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

    public String getNote() {
        return note;
    }

    public String getSettlementTransactionNumber() {
        return settlementTransactionNumber;
    }

    public Instant getSettledAt() {
        return settledAt;
    }
}
