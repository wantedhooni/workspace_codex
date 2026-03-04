package com.revy.mvpbanking.stock.domain;

import com.revy.mvpbanking.common.domain.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "stock_positions")
public class StockPosition extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 20)
    private String market;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "average_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal averagePrice;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "realized_profit_loss", nullable = false, precision = 19, scale = 4)
    private BigDecimal realizedProfitLoss;

    protected StockPosition() {
    }

    public StockPosition(
            UUID customerId,
            UUID accountId,
            String symbol,
            String market,
            BigDecimal quantity,
            BigDecimal averagePrice,
            String currency
    ) {
        this.customerId = customerId;
        this.accountId = accountId;
        this.symbol = symbol;
        this.market = market;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.currency = currency;
        this.realizedProfitLoss = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    }

    public void buy(BigDecimal executedQuantity, BigDecimal executedPrice) {
        if (executedQuantity == null || executedQuantity.signum() <= 0 || executedPrice == null || executedPrice.signum() <= 0) {
            throw new IllegalArgumentException("Executed quantity and price must be positive");
        }

        BigDecimal currentCost = this.averagePrice.multiply(this.quantity);
        BigDecimal newCost = currentCost.add(executedPrice.multiply(executedQuantity));
        BigDecimal newQuantity = this.quantity.add(executedQuantity);

        this.quantity = newQuantity;
        this.averagePrice = newCost.divide(newQuantity, 4, RoundingMode.HALF_UP);
    }

    public void sell(BigDecimal executedQuantity, BigDecimal executedPrice) {
        if (executedQuantity == null || executedQuantity.signum() <= 0 || executedPrice == null || executedPrice.signum() <= 0) {
            throw new IllegalArgumentException("Executed quantity and price must be positive");
        }
        if (this.quantity.compareTo(executedQuantity) < 0) {
            throw new IllegalStateException("Insufficient stock position quantity");
        }

        this.quantity = this.quantity.subtract(executedQuantity);
        BigDecimal pnl = executedPrice.subtract(this.averagePrice)
                .multiply(executedQuantity)
                .setScale(4, RoundingMode.HALF_UP);
        this.realizedProfitLoss = this.realizedProfitLoss.add(pnl);

        if (this.quantity.signum() == 0) {
            this.averagePrice = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public UUID getAccountId() { return accountId; }
    public String getSymbol() { return symbol; }
    public String getMarket() { return market; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getAveragePrice() { return averagePrice; }
    public String getCurrency() { return currency; }
    public BigDecimal getRealizedProfitLoss() { return realizedProfitLoss; }
}
