package com.derivops.mvp.opscase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ops_cases")
public class OpsCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String caseNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OpsCaseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OpsCaseSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OpsCaseStatus status;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(length = 50)
    private String assignee;

    private OffsetDateTime dueAt;

    @Column(length = 40)
    private String linkedType;

    @Column(length = 80)
    private String linkedId;

    private Long accountId;

    @Column(length = 1000)
    private String resolutionSummary;

    @Column(nullable = false, length = 50)
    private String createdBy;

    @Column(length = 50)
    private String updatedBy;

    @Column(length = 50)
    private String resolvedBy;

    private OffsetDateTime resolvedAt;

    @Column(length = 50)
    private String closedBy;

    private OffsetDateTime closedAt;

    @CreationTimestamp
    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
