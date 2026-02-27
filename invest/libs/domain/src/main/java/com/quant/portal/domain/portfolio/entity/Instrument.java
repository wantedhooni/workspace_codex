package com.quant.portal.domain.portfolio.entity;

import com.quant.portal.common.jpa.BaseAuditUserEntity;
import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import com.quant.portal.domain.portfolio.enums.MarketCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;

@Entity
@Table(
        name = "instruments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_instruments_market_ticker", columnNames = {"market_code", "ticker"})
        }
)
public class Instrument extends BaseAuditUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "market_code", nullable = false, length = 10)
    private MarketCode marketCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency_code", nullable = false, length = 10)
    private CurrencyCode currencyCode;

    protected Instrument() {
    }

    public Instrument(String ticker, String name, MarketCode marketCode, CurrencyCode currencyCode) {
        this.ticker = requireText(ticker, "ticker");
        this.name = requireText(name, "name");
        this.marketCode = Objects.requireNonNull(marketCode, "marketCode must not be null");
        this.currencyCode = Objects.requireNonNull(currencyCode, "currencyCode must not be null");
    }

    public Long getId() {
        return id;
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }

    public MarketCode getMarketCode() {
        return marketCode;
    }

    public CurrencyCode getCurrencyCode() {
        return currencyCode;
    }

    public void rename(String name) {
        this.name = requireText(name, "name");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
