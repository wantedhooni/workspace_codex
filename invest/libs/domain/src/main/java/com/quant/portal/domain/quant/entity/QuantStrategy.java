package com.quant.portal.domain.quant.entity;

import com.quant.portal.common.jpa.BaseAuditUserEntity;
import com.quant.portal.domain.quant.enums.QuantStyle;
import com.quant.portal.domain.quant.enums.StrategyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "quant_strategies")
public class QuantStrategy extends BaseAuditUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "style", nullable = false, length = 30)
    private QuantStyle style;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StrategyStatus status;

    @Column(name = "rebalance_cycle_days", nullable = false)
    private Integer rebalanceCycleDays;

    @Column(name = "description", length = 2000)
    private String description;

    protected QuantStrategy() {
    }

    public QuantStrategy(
            String name,
            QuantStyle style,
            StrategyStatus status,
            Integer rebalanceCycleDays,
            String description
    ) {
        this.name = requireName(name);
        this.style = Objects.requireNonNull(style, "style must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.rebalanceCycleDays = requirePositive(rebalanceCycleDays, "rebalanceCycleDays");
        this.description = normalizeDescription(description);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public QuantStyle getStyle() {
        return style;
    }

    public StrategyStatus getStatus() {
        return status;
    }

    public Integer getRebalanceCycleDays() {
        return rebalanceCycleDays;
    }

    public String getDescription() {
        return description;
    }

    public void update(
            String name,
            QuantStyle style,
            StrategyStatus status,
            Integer rebalanceCycleDays,
            String description
    ) {
        this.name = requireName(name);
        this.style = Objects.requireNonNull(style, "style must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.rebalanceCycleDays = requirePositive(rebalanceCycleDays, "rebalanceCycleDays");
        this.description = normalizeDescription(description);
    }

    private static String requireName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return value;
    }

    private static int requirePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
        return value;
    }

    private static String normalizeDescription(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
