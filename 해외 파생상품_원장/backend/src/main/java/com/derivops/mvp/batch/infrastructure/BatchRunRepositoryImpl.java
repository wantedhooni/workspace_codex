package com.derivops.mvp.batch.infrastructure;
import com.derivops.mvp.batch.*;
import com.derivops.mvp.batch.api.*;
import com.derivops.mvp.batch.application.*;
import com.derivops.mvp.batch.dto.*;
import com.derivops.mvp.batch.config.*;
import com.derivops.mvp.batch.job.*;


import com.derivops.mvp.common.rsql.QuerydslRsqlPredicateBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class BatchRunRepositoryImpl implements BatchRunRepositoryCustom {

    private static final QBatchRun batchRun = QBatchRun.batchRun;

    private static final Map<String, Expression<?>> FILTER_FIELDS = Map.ofEntries(
            Map.entry("id", batchRun.id),
            Map.entry("batchName", batchRun.batchName),
            Map.entry("status", batchRun.status),
            Map.entry("startedAt", batchRun.startedAt),
            Map.entry("finishedAt", batchRun.finishedAt),
            Map.entry("errorMessage", batchRun.errorMessage),
            Map.entry("retryCount", batchRun.retryCount)
    );

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<BatchRun> search(LocalDate date, BatchStatus status, String keyword, String filter, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        if (date != null) {
            OffsetDateTime from = date.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            OffsetDateTime to = date.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            builder.and(batchRun.startedAt.goe(from));
            builder.and(batchRun.startedAt.lt(to));
        }
        if (status != null) {
            builder.and(batchRun.status.eq(status));
        }
        if (keyword != null && !keyword.isBlank()) {
            String q = keyword.trim();
            builder.and(batchRun.batchName.containsIgnoreCase(q).or(batchRun.errorMessage.containsIgnoreCase(q)));
        }
        if (filter != null && !filter.isBlank()) {
            builder.and(QuerydslRsqlPredicateBuilder.build(filter, FILTER_FIELDS));
        }

        List<BatchRun> items = queryFactory
                .selectFrom(batchRun)
                .where(builder)
                .orderBy(batchRun.startedAt.desc(), batchRun.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(batchRun.count())
                .from(batchRun)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(items, pageable, total == null ? 0L : total);
    }
}
