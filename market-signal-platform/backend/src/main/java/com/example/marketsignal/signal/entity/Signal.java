package com.example.marketsignal.signal;

import com.example.marketsignal.common.BaseTimeEntity;
import com.example.marketsignal.report.DailyReport;
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
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 계산된 종목 시그널을 저장한다.
 */
@Getter
@Entity
@Table(name = "signals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Signal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SignalAction action;

    @Column(nullable = false, length = 300)
    private String reasons;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private DailyReport report;

    @Builder
    public Signal(LocalDate snapshotDate, String ticker, int score, SignalAction action, String reasons, DailyReport report) {
        this.snapshotDate = snapshotDate;
        this.ticker = ticker;
        this.score = score;
        this.action = action;
        this.reasons = reasons;
        this.report = report;
    }

    /**
     * 리포트와 시그널을 연결한다.
     */
    public void assignReport(DailyReport report) {
        this.report = report;
    }
}
