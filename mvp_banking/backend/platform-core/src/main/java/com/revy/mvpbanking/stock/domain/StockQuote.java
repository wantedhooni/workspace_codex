package com.revy.mvpbanking.stock.domain;

import com.revy.mvpbanking.common.domain.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_quotes")
public class StockQuote extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 20)
    private String market;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "change_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal changeRate;

    @Column(name = "effective_at", nullable = false)
    private Instant effectiveAt;

    @Column(nullable = false, length = 80)
    private String source;

    protected StockQuote() {
    }

    public StockQuote(
            String symbol,
            String market,
            BigDecimal price,
            String currency,
            BigDecimal changeRate,
            Instant effectiveAt,
            String source
    ) {
        this.symbol = symbol;
        this.market = market;
        this.price = price;
        this.currency = currency;
        this.changeRate = changeRate;
        this.effectiveAt = effectiveAt;
        this.source = source;
    }

    public UUID getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getMarket() {
        return market;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getChangeRate() {
        return changeRate;
    }

    public Instant getEffectiveAt() {
        return effectiveAt;
    }

    public String getSource() {
        return source;
    }
}
