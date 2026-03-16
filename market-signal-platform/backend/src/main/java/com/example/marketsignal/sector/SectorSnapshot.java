package com.example.marketsignal.sector;

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
 * 섹터 강도 스냅샷을 저장한다.
 */
@Getter
@Entity
@Table(name = "sector_snapshots")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SectorSnapshot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    @Column(nullable = false, length = 60)
    private String sectorName;

    @Column(nullable = false)
    private double strengthScore;

    @Builder
    public SectorSnapshot(LocalDate snapshotDate, String sectorName, double strengthScore) {
        this.snapshotDate = snapshotDate;
        this.sectorName = sectorName;
        this.strengthScore = strengthScore;
    }
}
