package com.quant.portal.domain.portfolio.entity;

import com.quant.portal.common.jpa.BaseAuditUserEntity;
import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import com.quant.portal.domain.portfolio.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "portfolio_transactions")
public class PortfolioTransaction extends BaseAuditUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id")
    private Instrument instrument;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(name = "quantity", precision = 19, scale = 8)
    private BigDecimal quantity;

    @Column(name = "unit_price", precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "fee", nullable = false, precision = 19, scale = 4)
    private BigDecimal fee;

    @Column(name = "tax", nullable = false, precision = 19, scale = 4)
    private BigDecimal tax;

    @Column(name = "cash_impact", nullable = false, precision = 19, scale = 4)
    private BigDecimal cashImpact;

    @Column(name = "realized_pnl", precision = 19, scale = 4)
    private BigDecimal realizedPnl;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency_code", nullable = false, length = 10)
    private CurrencyCode currencyCode;

    @Column(name = "memo", length = 500)
    private String memo;

    protected PortfolioTransaction() {
    }

    public PortfolioTransaction(
            Portfolio portfolio,
            Instrument instrument,
            TransactionType transactionType,
            LocalDate tradeDate,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal amount,
            BigDecimal fee,
            BigDecimal tax,
            BigDecimal cashImpact,
            BigDecimal realizedPnl,
            CurrencyCode currencyCode,
            String memo
    ) {
        this.portfolio = Objects.requireNonNull(portfolio, "portfolio must not be null");
        this.instrument = instrument;
        this.transactionType = Objects.requireNonNull(transactionType, "transactionType must not be null");
        this.tradeDate = Objects.requireNonNull(tradeDate, "tradeDate must not be null");
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.fee = Objects.requireNonNull(fee, "fee must not be null");
        this.tax = Objects.requireNonNull(tax, "tax must not be null");
        this.cashImpact = Objects.requireNonNull(cashImpact, "cashImpact must not be null");
        this.realizedPnl = realizedPnl;
        this.currencyCode = Objects.requireNonNull(currencyCode, "currencyCode must not be null");
        this.memo = memo;
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

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public BigDecimal getCashImpact() {
        return cashImpact;
    }

    public BigDecimal getRealizedPnl() {
        return realizedPnl;
    }

    public CurrencyCode getCurrencyCode() {
        return currencyCode;
    }

    public String getMemo() {
        return memo;
    }

    public void update(
            Instrument instrument,
            LocalDate tradeDate,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal amount,
            BigDecimal fee,
            BigDecimal tax,
            BigDecimal cashImpact,
            BigDecimal realizedPnl,
            CurrencyCode currencyCode,
            String memo
    ) {
        this.instrument = instrument;
        this.tradeDate = Objects.requireNonNull(tradeDate, "tradeDate must not be null");
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.fee = Objects.requireNonNull(fee, "fee must not be null");
        this.tax = Objects.requireNonNull(tax, "tax must not be null");
        this.cashImpact = Objects.requireNonNull(cashImpact, "cashImpact must not be null");
        this.realizedPnl = realizedPnl;
        this.currencyCode = Objects.requireNonNull(currencyCode, "currencyCode must not be null");
        this.memo = memo;
    }
}
