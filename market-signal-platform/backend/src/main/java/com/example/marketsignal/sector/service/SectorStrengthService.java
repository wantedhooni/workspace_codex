package com.example.marketsignal.sector;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 일자별 상위 섹터를 조회한다.
 */
@Service
@RequiredArgsConstructor
public class SectorStrengthService {

    private final SectorSnapshotRepository sectorSnapshotRepository;

    /**
     * 지정 일자의 선도 섹터를 상위 개수만큼 반환한다.
     */
    @Transactional(readOnly = true)
    public List<String> getLeadingSectors(LocalDate snapshotDate, int limit) {
        return sectorSnapshotRepository.findAllBySnapshotDateOrderByStrengthScoreDesc(snapshotDate).stream()
                .limit(limit)
                .map(SectorSnapshot::getSectorName)
                .toList();
    }
}
