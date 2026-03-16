package com.example.marketsignal.stock;

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
 * 종목 시그널 계산용 스냅샷을 저장한다.
 */
@Getter
@Entity
@Table(name = "stock_snapshots")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockSnapshot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Column(nullable = false, length = 120)
    private String companyName;

    @Column(nullable = false)
    private boolean above20Dma;

    @Column(nullable = false)
    private boolean above50Dma;

    @Column(nullable = false)
    private boolean relativeStrengthStrong;

    @Column(nullable = false)
    private boolean earningsReactionPositive;

    @Column(nullable = false)
    private boolean volumeSurge;

    @Column(nullable = false)
    private boolean negativeNewsWeakPrice;

    @Builder
    public StockSnapshot(
            LocalDate snapshotDate,
            String ticker,
            String companyName,
            boolean above20Dma,
            boolean above50Dma,
            boolean relativeStrengthStrong,
            boolean earningsReactionPositive,
            boolean volumeSurge,
            boolean negativeNewsWeakPrice
    ) {
        this.snapshotDate = snapshotDate;
        this.ticker = ticker;
        this.companyName = companyName;
        this.above20Dma = above20Dma;
        this.above50Dma = above50Dma;
        this.relativeStrengthStrong = relativeStrengthStrong;
        this.earningsReactionPositive = earningsReactionPositive;
        this.volumeSurge = volumeSurge;
        this.negativeNewsWeakPrice = negativeNewsWeakPrice;
    }
}
