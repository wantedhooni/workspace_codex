package com.example.marketsignal.macro;

import com.example.marketsignal.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시장 레짐 계산에 필요한 매크로 스냅샷을 저장한다.
 */
@Getter
@Entity
@Table(name = "macro_snapshots")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MacroSnapshot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate snapshotDate;

    @Column(nullable = false)
    private double tenYearYieldChange;

    @Column(nullable = false)
    private double dxyChange;

    @Column(nullable = false)
    private double oilChange;

    @Column(nullable = false)
    private double futuresChange;

    @Builder
    public MacroSnapshot(
            LocalDate snapshotDate,
            double tenYearYieldChange,
            double dxyChange,
            double oilChange,
            double futuresChange
    ) {
        this.snapshotDate = snapshotDate;
        this.tenYearYieldChange = tenYearYieldChange;
        this.dxyChange = dxyChange;
        this.oilChange = oilChange;
        this.futuresChange = futuresChange;
    }
}
