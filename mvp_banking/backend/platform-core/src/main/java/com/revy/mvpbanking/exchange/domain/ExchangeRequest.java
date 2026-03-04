package com.revy.mvpbanking.exchange.domain;

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
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exchange_requests")
public class ExchangeRequest extends BaseJpaEntity {

    private static final BigDecimal EXCHANGE_FEE_RATE = new BigDecimal("0.0012");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "source_account_id")
    private UUID sourceAccountId;

    @Column(name = "account_id", nullable = false)
    private UUID destinationAccountId;

    @Column(name = "request_number", nullable = false, unique = true, length = 40)
    private String requestNumber;

    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false, length = 3)
    private String toCurrency;

    @Column(name = "from_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal fromAmount;

    @Column(name = "applied_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal appliedRate;

    @Column(name = "to_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal toAmount;

    @Column(name = "exchange_fee_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal exchangeFeeAmount;

    @Column(name = "net_to_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal netToAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExchangeRequestStatus status;

    @Column(name = "source_transaction_number", length = 50)
    private String sourceTransactionNumber;

    @Column(name = "settlement_transaction_number", length = 50)
    private String destinationTransactionNumber;

    @Column(name = "settled_at")
    private Instant settledAt;

    protected ExchangeRequest() {
    }

    public ExchangeRequest(
            UUID customerId,
            UUID sourceAccountId,
            UUID destinationAccountId,
            String requestNumber,
            String fromCurrency,
            String toCurrency,
            BigDecimal fromAmount,
            BigDecimal appliedRate,
            BigDecimal toAmount,
            ExchangeRequestStatus status
    ) {
        this.customerId = customerId;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.requestNumber = requestNumber;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.fromAmount = fromAmount;
        this.appliedRate = appliedRate;
        this.toAmount = toAmount;
        this.exchangeFeeAmount = calculateExchangeFee(toAmount);
        this.netToAmount = toAmount.subtract(this.exchangeFeeAmount).setScale(4, RoundingMode.HALF_UP);
        this.status = status;
    }

    public void synchronizeAccounts(UUID sourceAccountId, UUID destinationAccountId) {
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
    }

    public void approve(String sourceTransactionNumber, String destinationTransactionNumber, Instant settledAt) {
        this.status = ExchangeRequestStatus.APPROVED;
        this.sourceTransactionNumber = sourceTransactionNumber;
        this.destinationTransactionNumber = destinationTransactionNumber;
        this.settledAt = settledAt;
    }

    public void reject() {
        this.status = ExchangeRequestStatus.REJECTED;
        this.sourceTransactionNumber = null;
        this.destinationTransactionNumber = null;
        this.settledAt = null;
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public UUID getSourceAccountId() { return sourceAccountId; }
    public UUID getDestinationAccountId() { return destinationAccountId; }
    public String getRequestNumber() { return requestNumber; }
    public String getFromCurrency() { return fromCurrency; }
    public String getToCurrency() { return toCurrency; }
    public BigDecimal getFromAmount() { return fromAmount; }
    public BigDecimal getAppliedRate() { return appliedRate; }
    public BigDecimal getToAmount() { return toAmount; }
    public BigDecimal getExchangeFeeAmount() { return exchangeFeeAmount; }
    public BigDecimal getNetToAmount() { return netToAmount; }
    public ExchangeRequestStatus getStatus() { return status; }
    public String getSourceTransactionNumber() { return sourceTransactionNumber; }
    public String getDestinationTransactionNumber() { return destinationTransactionNumber; }
    public Instant getSettledAt() { return settledAt; }

    public UUID getAccountId() { return destinationAccountId; }
    public String getSettlementTransactionNumber() { return destinationTransactionNumber; }

    private static BigDecimal calculateExchangeFee(BigDecimal grossToAmount) {
        return grossToAmount.multiply(EXCHANGE_FEE_RATE).setScale(4, RoundingMode.HALF_UP);
    }
}
