package com.derivops.mvp.journalentry;

import com.derivops.mvp.account.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "journal_entries")
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String journalNo;

    @Column(nullable = false)
    private Integer lineNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, length = 30)
    private String referenceType;

    @Column(nullable = false, length = 50)
    private String referenceId;

    @Column(nullable = false)
    private LocalDate postingDate;

    @Column(nullable = false, length = 40)
    private String accountCode;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal debitAmount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal creditAmount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false, length = 500)
    private String description;

    @CreationTimestamp
    @Column(nullable = false)
    private OffsetDateTime createdAt;
}
