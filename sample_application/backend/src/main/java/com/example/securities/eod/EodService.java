package com.example.securities.eod;

import static com.example.securities.account.QAccount.account;

import com.example.securities.common.BusinessException;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EodService {

    private final EodSnapshotRepository eodSnapshotRepository;
    private final JPAQueryFactory jpaQueryFactory;

    public EodService(EodSnapshotRepository eodSnapshotRepository, JPAQueryFactory jpaQueryFactory) {
        this.eodSnapshotRepository = eodSnapshotRepository;
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Transactional
    public EodDtos.EodSnapshotResponse run(LocalDate businessDate) {
        LocalDate targetDate = businessDate == null ? LocalDate.now() : businessDate;

        eodSnapshotRepository.findByBusinessDate(targetDate).ifPresent(existing -> {
            throw new BusinessException("EOD_ALREADY_EXISTS", "해당 영업일 EOD 스냅샷이 이미 생성되었습니다.");
        });

        Tuple tuple = jpaQueryFactory
                .select(account.id.count(), account.balance.sum())
                .from(account)
                .fetchOne();

        long accountCount = tuple != null && tuple.get(account.id.count()) != null ? tuple.get(account.id.count()) : 0L;
        BigDecimal totalBalance = tuple != null && tuple.get(account.balance.sum()) != null
                ? tuple.get(account.balance.sum())
                : BigDecimal.ZERO;

        String status = totalBalance.compareTo(BigDecimal.ZERO) >= 0 ? "MATCHED" : "MISMATCH";

        EodSnapshot saved = eodSnapshotRepository.save(
                new EodSnapshot(targetDate, accountCount, totalBalance, status)
        );

        return new EodDtos.EodSnapshotResponse(
                saved.getId(),
                saved.getBusinessDate(),
                saved.getAccountCount(),
                saved.getTotalBalance(),
                saved.getReconciliationStatus(),
                saved.getCreatedAt()
        );
    }

    @Scheduled(cron = "${app.eod.cron:0 0 23 * * *}")
    public void scheduledRun() {
        try {
            run(LocalDate.now());
        } catch (BusinessException ignored) {
            // 이미 생성된 날짜면 스킵
        }
    }

    @Transactional(readOnly = true)
    public List<EodDtos.EodSnapshotResponse> getSnapshots() {
        return eodSnapshotRepository.findAll().stream()
                .map(s -> new EodDtos.EodSnapshotResponse(
                        s.getId(),
                        s.getBusinessDate(),
                        s.getAccountCount(),
                        s.getTotalBalance(),
                        s.getReconciliationStatus(),
                        s.getCreatedAt()))
                .toList();
    }
}
