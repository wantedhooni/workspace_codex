package com.derivops.mvp.risk;

import com.derivops.mvp.approval.ApprovalDomain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "risk_limit_policies")
public class RiskLimitPolicy {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 30)
    private String brokerCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApprovalDomain domain;

    @Column(length = 3)
    private String currencyCode;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal maxPerRequest;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal dailySoftLimit;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal dailyHardLimit;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Column(length = 300)
    private String description;

    @CreationTimestamp
    @Column(nullable = false)
    private OffsetDateTime createdAt;
}
