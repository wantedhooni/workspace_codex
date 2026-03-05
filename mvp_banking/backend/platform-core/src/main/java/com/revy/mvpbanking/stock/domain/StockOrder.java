package com.revy.mvpbanking.stock.domain;

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
@Table(name = "stock_orders")
public class StockOrder extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "order_number", nullable = false, unique = true, length = 40)
    private String orderNumber;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 20)
    private String market;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StockOrderSide side;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "limit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal limitPrice;

    @Column(name = "gross_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal grossAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StockOrderStatus status;

    @Column(name = "executed_quantity", precision = 19, scale = 4)
    private BigDecimal executedQuantity;

    @Column(name = "executed_price", precision = 19, scale = 4)
    private BigDecimal executedPrice;

    @Column(name = "remaining_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal remainingQuantity;

    @Column(name = "fee_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal feeAmount;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxAmount;

    @Column(name = "net_settlement_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal netSettlementAmount;

    @Column(name = "settlement_transaction_number", length = 50)
    private String settlementTransactionNumber;

    @Column(name = "order_memo", length = 200)
    private String orderMemo;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_in_force", nullable = false, length = 20)
    private StockOrderTimeInForce timeInForce;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "market_session", nullable = false, length = 30)
    private StockOrderMarketSession marketSession;

    @Column(name = "expected_execution_at", nullable = false)
    private Instant expectedExecutionAt;

    @Column(name = "manual_review_required", nullable = false)
    private boolean manualReviewRequired;

    @Column(name = "manual_review_reason", length = 255)
    private String manualReviewReason;

    @Column(name = "reference_price", precision = 19, scale = 4)
    private BigDecimal referencePrice;

    @Column(name = "price_deviation_rate", precision = 19, scale = 6)
    private BigDecimal priceDeviationRate;

    @Column(name = "quote_effective_at")
    private Instant quoteEffectiveAt;

    @Column(name = "quote_source", length = 80)
    private String quoteSource;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;

    @Column(name = "canceled_at")
    private Instant canceledAt;

    @Column(name = "settled_at")
    private Instant settledAt;

    protected StockOrder() {
    }

    public StockOrder(
            UUID customerId,
            UUID accountId,
            String orderNumber,
            String symbol,
            String market,
            StockOrderSide side,
            BigDecimal quantity,
            BigDecimal limitPrice,
            BigDecimal grossAmount,
            String currency,
            StockOrderStatus status
    ) {
        this(
                customerId,
                accountId,
                orderNumber,
                symbol,
                market,
                side,
                quantity,
                limitPrice,
                grossAmount,
                currency,
                status,
                null
        );
    }

    public StockOrder(
            UUID customerId,
            UUID accountId,
            String orderNumber,
            String symbol,
            String market,
            StockOrderSide side,
            BigDecimal quantity,
            BigDecimal limitPrice,
            BigDecimal grossAmount,
            String currency,
            StockOrderStatus status,
            String orderMemo
    ) {
        this(
                customerId,
                accountId,
                orderNumber,
                symbol,
                market,
                side,
                quantity,
                limitPrice,
                grossAmount,
                currency,
                status,
                orderMemo,
                StockOrderTimeInForce.DAY,
                Instant.now().plusSeconds(86_400),
                StockOrderMarketSession.REGULAR,
                Instant.now().plusSeconds(300),
                false,
                null,
                null,
                null,
                null,
                null
        );
    }

    public StockOrder(
            UUID customerId,
            UUID accountId,
            String orderNumber,
            String symbol,
            String market,
            StockOrderSide side,
            BigDecimal quantity,
            BigDecimal limitPrice,
            BigDecimal grossAmount,
            String currency,
            StockOrderStatus status,
            String orderMemo,
            StockOrderTimeInForce timeInForce,
            Instant expiresAt,
            StockOrderMarketSession marketSession,
            Instant expectedExecutionAt,
            boolean manualReviewRequired,
            String manualReviewReason,
            BigDecimal referencePrice,
            BigDecimal priceDeviationRate,
            Instant quoteEffectiveAt,
            String quoteSource
    ) {
        this.customerId = customerId;
        this.accountId = accountId;
        this.orderNumber = orderNumber;
        this.symbol = symbol;
        this.market = market;
        this.side = side;
        this.quantity = quantity;
        this.limitPrice = limitPrice;
        this.grossAmount = grossAmount;
        this.currency = currency;
        this.status = status;
        this.remainingQuantity = quantity;
        this.feeAmount = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        this.taxAmount = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        this.netSettlementAmount = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        this.orderMemo = orderMemo;
        this.timeInForce = timeInForce;
        this.expiresAt = expiresAt;
        this.marketSession = marketSession;
        this.expectedExecutionAt = expectedExecutionAt;
        this.manualReviewRequired = manualReviewRequired;
        this.manualReviewReason = manualReviewReason;
        this.referencePrice = referencePrice;
        this.priceDeviationRate = priceDeviationRate;
        this.quoteEffectiveAt = quoteEffectiveAt;
        this.quoteSource = quoteSource;
    }

    public void applyExecution(
            BigDecimal cumulativeExecutedQuantity,
            BigDecimal cumulativeExecutedPrice,
            BigDecimal cumulativeFeeAmount,
            BigDecimal cumulativeTaxAmount,
            BigDecimal cumulativeNetSettlementAmount,
            String settlementTransactionNumber,
            Instant settledAt
    ) {
        if (cumulativeExecutedQuantity == null || cumulativeExecutedQuantity.signum() <= 0) {
            throw new IllegalArgumentException("Executed quantity must be positive");
        }
        if (cumulativeExecutedQuantity.compareTo(this.quantity) > 0) {
            throw new IllegalArgumentException("Executed quantity cannot exceed order quantity");
        }

        this.executedQuantity = cumulativeExecutedQuantity.setScale(4, RoundingMode.HALF_UP);
        this.executedPrice = cumulativeExecutedPrice.setScale(4, RoundingMode.HALF_UP);
        this.remainingQuantity = this.quantity.subtract(this.executedQuantity).setScale(4, RoundingMode.HALF_UP);
        this.feeAmount = cumulativeFeeAmount.setScale(4, RoundingMode.HALF_UP);
        this.taxAmount = cumulativeTaxAmount.setScale(4, RoundingMode.HALF_UP);
        this.netSettlementAmount = cumulativeNetSettlementAmount.setScale(4, RoundingMode.HALF_UP);
        this.settlementTransactionNumber = settlementTransactionNumber;
        this.settledAt = settledAt;
        this.status = this.remainingQuantity.signum() == 0
                ? StockOrderStatus.APPROVED
                : StockOrderStatus.PARTIALLY_FILLED;
    }

    public void reject() {
        this.status = StockOrderStatus.REJECTED;
        this.executedQuantity = null;
        this.executedPrice = null;
        this.remainingQuantity = this.quantity;
        this.feeAmount = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        this.taxAmount = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        this.netSettlementAmount = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        this.settlementTransactionNumber = null;
        this.settledAt = null;
    }

    public void cancel(String cancellationReason) {
        if (this.status == StockOrderStatus.CANCELED) {
            return;
        }
        if (this.status == StockOrderStatus.APPROVED) {
            throw new IllegalStateException("Approved stock order cannot be canceled");
        }
        if (this.status == StockOrderStatus.REJECTED) {
            throw new IllegalStateException("Rejected stock order cannot be canceled");
        }
        this.status = StockOrderStatus.CANCELED;
        this.cancellationReason = cancellationReason;
        this.canceledAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public UUID getAccountId() { return accountId; }
    public String getOrderNumber() { return orderNumber; }
    public String getSymbol() { return symbol; }
    public String getMarket() { return market; }
    public StockOrderSide getSide() { return side; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getLimitPrice() { return limitPrice; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public String getCurrency() { return currency; }
    public StockOrderStatus getStatus() { return status; }
    public BigDecimal getExecutedQuantity() { return executedQuantity; }
    public BigDecimal getExecutedPrice() { return executedPrice; }
    public BigDecimal getRemainingQuantity() { return remainingQuantity; }
    public BigDecimal getFeeAmount() { return feeAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getNetSettlementAmount() { return netSettlementAmount; }
    public String getSettlementTransactionNumber() { return settlementTransactionNumber; }
    public String getOrderMemo() { return orderMemo; }
    public StockOrderTimeInForce getTimeInForce() { return timeInForce; }
    public Instant getExpiresAt() { return expiresAt; }
    public StockOrderMarketSession getMarketSession() { return marketSession; }
    public Instant getExpectedExecutionAt() { return expectedExecutionAt; }
    public boolean isManualReviewRequired() { return manualReviewRequired; }
    public String getManualReviewReason() { return manualReviewReason; }
    public BigDecimal getReferencePrice() { return referencePrice; }
    public BigDecimal getPriceDeviationRate() { return priceDeviationRate; }
    public Instant getQuoteEffectiveAt() { return quoteEffectiveAt; }
    public String getQuoteSource() { return quoteSource; }
    public String getCancellationReason() { return cancellationReason; }
    public Instant getCanceledAt() { return canceledAt; }
    public Instant getSettledAt() { return settledAt; }
}
