package com.revy.mvpbanking.fx.domain;

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
@Table(name = "fx_rates")
public class FxRate extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    @Column(name = "quote_currency", nullable = false, length = 3)
    private String quoteCurrency;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal rate;

    @Column(name = "effective_at", nullable = false)
    private Instant effectiveAt;

    @Column(nullable = false, length = 80)
    private String source;

    protected FxRate() {
    }

    public FxRate(String baseCurrency, String quoteCurrency, BigDecimal rate, Instant effectiveAt, String source) {
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.rate = rate;
        this.effectiveAt = effectiveAt;
        this.source = source;
    }

    public UUID getId() {
        return id;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public Instant getEffectiveAt() {
        return effectiveAt;
    }

    public String getSource() {
        return source;
    }
}
