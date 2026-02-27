package com.quant.portal.domain.macro.entity;

import com.quant.portal.common.jpa.BaseAuditUserEntity;
import com.quant.portal.domain.macro.enums.MacroRegionCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(
        name = "macro_indicators",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_macro_indicator_code_region_date",
                        columnNames = {"indicator_code", "region_code", "observed_date"}
                )
        }
)
public class MacroIndicator extends BaseAuditUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "indicator_code", nullable = false, length = 40)
    private String indicatorCode;

    @Column(name = "indicator_name", nullable = false, length = 200)
    private String indicatorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "region_code", nullable = false, length = 20)
    private MacroRegionCode regionCode;

    @Column(name = "observed_date", nullable = false)
    private LocalDate observedDate;

    @Column(name = "indicator_value", nullable = false, precision = 19, scale = 6)
    private BigDecimal indicatorValue;

    @Column(name = "unit", length = 40)
    private String unit;

    @Column(name = "source", length = 200)
    private String source;

    protected MacroIndicator() {
    }

    public MacroIndicator(
            String indicatorCode,
            String indicatorName,
            MacroRegionCode regionCode,
            LocalDate observedDate,
            BigDecimal indicatorValue,
            String unit,
            String source
    ) {
        this.indicatorCode = requireText(indicatorCode, "indicatorCode");
        this.indicatorName = requireText(indicatorName, "indicatorName");
        this.regionCode = Objects.requireNonNull(regionCode, "regionCode must not be null");
        this.observedDate = Objects.requireNonNull(observedDate, "observedDate must not be null");
        this.indicatorValue = Objects.requireNonNull(indicatorValue, "indicatorValue must not be null");
        this.unit = normalizeOptional(unit);
        this.source = normalizeOptional(source);
    }

    public Long getId() {
        return id;
    }

    public String getIndicatorCode() {
        return indicatorCode;
    }

    public String getIndicatorName() {
        return indicatorName;
    }

    public MacroRegionCode getRegionCode() {
        return regionCode;
    }

    public LocalDate getObservedDate() {
        return observedDate;
    }

    public BigDecimal getIndicatorValue() {
        return indicatorValue;
    }

    public String getUnit() {
        return unit;
    }

    public String getSource() {
        return source;
    }

    public void update(
            String indicatorName,
            MacroRegionCode regionCode,
            LocalDate observedDate,
            BigDecimal indicatorValue,
            String unit,
            String source
    ) {
        this.indicatorName = requireText(indicatorName, "indicatorName");
        this.regionCode = Objects.requireNonNull(regionCode, "regionCode must not be null");
        this.observedDate = Objects.requireNonNull(observedDate, "observedDate must not be null");
        this.indicatorValue = Objects.requireNonNull(indicatorValue, "indicatorValue must not be null");
        this.unit = normalizeOptional(unit);
        this.source = normalizeOptional(source);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
