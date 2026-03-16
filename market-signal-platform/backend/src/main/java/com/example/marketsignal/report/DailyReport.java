package com.example.marketsignal.report;

import com.example.marketsignal.common.BaseTimeEntity;
import com.example.marketsignal.common.StringListConverter;
import com.example.marketsignal.macro.MarketRegime;
import com.example.marketsignal.signal.Signal;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 하루 기준 시장 분석 리포트를 저장한다.
 */
@Getter
@Entity
@Table(name = "reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate reportDate;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MarketRegime marketRegime;

    @Convert(converter = StringListConverter.class)
    @Column(nullable = false, length = 300)
    private List<String> leadingSectors = new ArrayList<>();

    @Column(nullable = false, length = 500)
    private String summary;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Signal> signals = new ArrayList<>();

    @Builder
    public DailyReport(
            LocalDate reportDate,
            LocalDate snapshotDate,
            MarketRegime marketRegime,
            List<String> leadingSectors,
            String summary
    ) {
        this.reportDate = reportDate;
        this.snapshotDate = snapshotDate;
        this.marketRegime = marketRegime;
        this.leadingSectors = new ArrayList<>(leadingSectors);
        this.summary = summary;
    }

    /**
     * 리포트에 시그널 목록을 반영한다.
     */
    public void replaceSignals(List<Signal> newSignals) {
        signals.clear();
        newSignals.forEach(signal -> signal.assignReport(this));
        signals.addAll(newSignals);
    }

    /**
     * 리포트 핵심 지표를 최신 값으로 갱신한다.
     */
    public void refresh(LocalDate snapshotDate, MarketRegime marketRegime, List<String> leadingSectors, String summary) {
        this.snapshotDate = snapshotDate;
        this.marketRegime = marketRegime;
        this.leadingSectors = new ArrayList<>(leadingSectors);
        this.summary = summary;
    }
}
