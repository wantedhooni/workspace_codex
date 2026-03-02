package com.derivops.mvp.cashfx;

import com.derivops.mvp.account.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
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
@Table(name = "cash_requests")
public class CashRequest {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CashRequestType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RequestPriority priority;

    private LocalDate valueDate;

    @Column(nullable = false)
    private boolean manualReviewRequired;

    @Column(length = 500)
    private String controlReason;

    private Long controlPolicyId;

    @Column(length = 30)
    private String controlPolicySource;

    private Long controlLimitPolicyId;

    @Column(length = 30)
    private String controlLimitPolicySource;

    @Column(precision = 19, scale = 4)
    private BigDecimal projectedDailyExposure;

    private OffsetDateTime slaDueAt;

    @Column(nullable = false, length = 300)
    private String reason;

    @Column(nullable = false, length = 50)
    private String requestedBy;

    @Column(length = 50)
    private String reviewedBy;

    @Column(length = 300)
    private String reviewReason;

    @CreationTimestamp
    @Column(nullable = false)
    private OffsetDateTime requestedAt;

    private OffsetDateTime reviewedAt;

    @Version
    private Long version;
}
