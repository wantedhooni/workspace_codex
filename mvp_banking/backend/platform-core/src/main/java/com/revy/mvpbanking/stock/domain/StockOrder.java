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
    public Instant getSettledAt() { return settledAt; }
}
