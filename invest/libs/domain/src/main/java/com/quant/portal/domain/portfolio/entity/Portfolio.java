package com.quant.portal.domain.portfolio.entity;

import com.quant.portal.common.jpa.BaseAuditUserEntity;
import com.quant.portal.domain.portfolio.enums.CurrencyCode;
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
import java.util.Objects;

@Entity
@Table(name = "portfolios")
public class Portfolio extends BaseAuditUserEntity {

    private static final int MONEY_SCALE = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_currency", nullable = false, length = 10)
    private CurrencyCode baseCurrency;

    @Column(name = "cash_balance", nullable = false, precision = 19, scale = MONEY_SCALE)
    private BigDecimal cashBalance;

    protected Portfolio() {
    }

    public Portfolio(String name, CurrencyCode baseCurrency) {
        this.name = requireName(name);
        this.baseCurrency = Objects.requireNonNull(baseCurrency, "baseCurrency must not be null");
        this.cashBalance = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CurrencyCode getBaseCurrency() {
        return baseCurrency;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void applyCashDelta(BigDecimal delta) {
        Objects.requireNonNull(delta, "delta must not be null");
        BigDecimal next = cashBalance.add(delta).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        if (next.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cash balance cannot be negative");
        }
        this.cashBalance = next;
    }

    public void rename(String name) {
        this.name = requireName(name);
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return name;
    }
}
