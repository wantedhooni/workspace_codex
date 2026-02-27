package com.quant.portal.domain.quant.entity;

import com.quant.portal.common.jpa.BaseAuditUserEntity;
import com.quant.portal.domain.portfolio.entity.Instrument;
import com.quant.portal.domain.quant.enums.SignalType;
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
@Table(name = "quant_signals")
public class QuantSignal extends BaseAuditUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "strategy_id", nullable = false)
    private QuantStrategy strategy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Enumerated(EnumType.STRING)
    @Column(name = "signal_type", nullable = false, length = 20)
    private SignalType signalType;

    @Column(name = "signal_date", nullable = false)
    private LocalDate signalDate;

    @Column(name = "score", nullable = false, precision = 19, scale = 6)
    private BigDecimal score;

    @Column(name = "confidence", nullable = false, precision = 8, scale = 4)
    private BigDecimal confidence;

    @Column(name = "rationale", length = 1000)
    private String rationale;

    protected QuantSignal() {
    }

    public QuantSignal(
            QuantStrategy strategy,
            Instrument instrument,
            SignalType signalType,
            LocalDate signalDate,
            BigDecimal score,
            BigDecimal confidence,
            String rationale
    ) {
        this.strategy = Objects.requireNonNull(strategy, "strategy must not be null");
        this.instrument = Objects.requireNonNull(instrument, "instrument must not be null");
        this.signalType = Objects.requireNonNull(signalType, "signalType must not be null");
        this.signalDate = Objects.requireNonNull(signalDate, "signalDate must not be null");
        this.score = Objects.requireNonNull(score, "score must not be null");
        this.confidence = Objects.requireNonNull(confidence, "confidence must not be null");
        this.rationale = normalizeRationale(rationale);
    }

    public Long getId() {
        return id;
    }

    public QuantStrategy getStrategy() {
        return strategy;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public SignalType getSignalType() {
        return signalType;
    }

    public LocalDate getSignalDate() {
        return signalDate;
    }

    public BigDecimal getScore() {
        return score;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public String getRationale() {
        return rationale;
    }

    public void update(
            Instrument instrument,
            SignalType signalType,
            LocalDate signalDate,
            BigDecimal score,
            BigDecimal confidence,
            String rationale
    ) {
        this.instrument = Objects.requireNonNull(instrument, "instrument must not be null");
        this.signalType = Objects.requireNonNull(signalType, "signalType must not be null");
        this.signalDate = Objects.requireNonNull(signalDate, "signalDate must not be null");
        this.score = Objects.requireNonNull(score, "score must not be null");
        this.confidence = Objects.requireNonNull(confidence, "confidence must not be null");
        this.rationale = normalizeRationale(rationale);
    }

    private static String normalizeRationale(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
