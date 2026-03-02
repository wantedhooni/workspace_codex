package com.derivops.mvp.approval;

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
import org.hibernate.envers.Audited;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "approval_policies")
public class ApprovalPolicy {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 30)
    private String brokerCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApprovalDomain domain;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal highThreshold;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal urgentThreshold;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal manualReviewThreshold;

    @Column(nullable = false)
    private boolean sameDayAutoReview;

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
