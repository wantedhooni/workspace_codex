package com.quant.portal.domain.portfolio.entity;

import com.quant.portal.common.jpa.BaseAuditUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Entity
@Table(
        name = "holdings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_holdings_portfolio_instrument", columnNames = {"portfolio_id", "instrument_id"})
        }
)
public class Holding extends BaseAuditUserEntity {

    private static final int MONEY_SCALE = 4;
    private static final int QUANTITY_SCALE = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(name = "quantity", nullable = false, precision = 19, scale = QUANTITY_SCALE)
    private BigDecimal quantity;

    @Column(name = "average_cost", nullable = false, precision = 19, scale = MONEY_SCALE)
    private BigDecimal averageCost;

    protected Holding() {
    }

    public Holding(Portfolio portfolio, Instrument instrument) {
        this.portfolio = Objects.requireNonNull(portfolio, "portfolio must not be null");
        this.instrument = Objects.requireNonNull(instrument, "instrument must not be null");
        this.quantity = BigDecimal.ZERO.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
        this.averageCost = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public void applyBuy(BigDecimal buyQuantity, BigDecimal totalBuyCost) {
        requirePositive(buyQuantity, "buyQuantity");
        requirePositive(totalBuyCost, "totalBuyCost");

        BigDecimal normalizedBuyQuantity = buyQuantity.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
        BigDecimal existingCost = this.averageCost.multiply(this.quantity);

        BigDecimal nextQuantity = this.quantity.add(normalizedBuyQuantity).setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
        BigDecimal nextTotalCost = existingCost.add(totalBuyCost);
        BigDecimal nextAverageCost = nextTotalCost.divide(nextQuantity, MONEY_SCALE, RoundingMode.HALF_UP);

        this.quantity = nextQuantity;
        this.averageCost = nextAverageCost;
    }

    public void applySell(BigDecimal sellQuantity) {
        requirePositive(sellQuantity, "sellQuantity");

        BigDecimal normalizedSellQuantity = sellQuantity.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
        if (normalizedSellQuantity.compareTo(this.quantity) > 0) {
            throw new IllegalArgumentException("sellQuantity cannot exceed current quantity");
        }

        BigDecimal nextQuantity = this.quantity.subtract(normalizedSellQuantity).setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
        this.quantity = nextQuantity;
        if (nextQuantity.compareTo(BigDecimal.ZERO) == 0) {
            this.averageCost = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
    }

    public boolean isEmpty() {
        return quantity.compareTo(BigDecimal.ZERO) == 0;
    }

    private static void requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
    }
}
