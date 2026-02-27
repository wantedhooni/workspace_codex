package com.portal.admin.repo;

import com.portal.admin.domain.BatchSchedule;
import com.portal.admin.domain.QBatchSchedule;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BatchScheduleRepositoryImpl implements SearchableRepository<BatchSchedule>, FieldSearchableRepository<BatchSchedule> {

    private final JPAQueryFactory queryFactory;

    public BatchScheduleRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<BatchSchedule> search(String query) {
        QBatchSchedule schedule = QBatchSchedule.batchSchedule;
        return queryFactory
                .selectFrom(schedule)
                .where(
                        schedule.cronExpression.containsIgnoreCase(query)
                                .or(schedule.timezone.containsIgnoreCase(query))
                                .or(schedule.lastStatus.containsIgnoreCase(query))
                                .or(schedule.lastRunAt.containsIgnoreCase(query))
                )
                .orderBy(schedule.id.asc())
                .fetch();
    }

    @Override
    public Page<BatchSchedule> search(String query, Pageable pageable) {
        QBatchSchedule schedule = QBatchSchedule.batchSchedule;
        BooleanBuilder where = new BooleanBuilder()
                .and(
                        schedule.cronExpression.containsIgnoreCase(query)
                                .or(schedule.timezone.containsIgnoreCase(query))
                                .or(schedule.lastStatus.containsIgnoreCase(query))
                                .or(schedule.lastRunAt.containsIgnoreCase(query))
                );
        return fetchPage(schedule, where, pageable);
    }

    @Override
    public List<BatchSchedule> searchByFields(Map<String, String> filters) {
        QBatchSchedule schedule = QBatchSchedule.batchSchedule;
        BooleanBuilder where = buildFieldWhere(schedule, filters);

        return queryFactory
                .selectFrom(schedule)
                .where(where)
                .orderBy(schedule.id.asc())
                .fetch();
    }

    @Override
    public Page<BatchSchedule> searchByFields(Map<String, String> filters, Pageable pageable) {
        QBatchSchedule schedule = QBatchSchedule.batchSchedule;
        BooleanBuilder where = buildFieldWhere(schedule, filters);
        return fetchPage(schedule, where, pageable);
    }

    @Override
    public Set<String> allowedSortFields() {
        return Set.of("id", "batchJobId", "cronExpression", "timezone", "enabled", "lastStatus", "lastRunAt");
    }

    @Override
    public Set<String> allowedFilterFields() {
        return Set.of("batchJobId", "cronExpression", "timezone", "enabled", "lastStatus", "lastRunAt");
    }

    private BooleanBuilder buildFieldWhere(QBatchSchedule schedule, Map<String, String> filters) {
        BooleanBuilder where = new BooleanBuilder();

        String batchJobId = filters.get("batchJobId");
        if (batchJobId != null) {
            try {
                where.and(schedule.batchJobId.eq(Long.parseLong(batchJobId.trim())));
            } catch (NumberFormatException ignored) {
            }
        }

        String cronExpression = filters.get("cronExpression");
        if (cronExpression != null && !cronExpression.isBlank()) {
            where.and(schedule.cronExpression.containsIgnoreCase(cronExpression.trim()));
        }

        String timezone = filters.get("timezone");
        if (timezone != null && !timezone.isBlank()) {
            where.and(schedule.timezone.containsIgnoreCase(timezone.trim()));
        }

        String lastStatus = filters.get("lastStatus");
        if (lastStatus != null && !lastStatus.isBlank()) {
            where.and(schedule.lastStatus.containsIgnoreCase(lastStatus.trim()));
        }

        String lastRunAt = filters.get("lastRunAt");
        if (lastRunAt != null && !lastRunAt.isBlank()) {
            where.and(schedule.lastRunAt.containsIgnoreCase(lastRunAt.trim()));
        }

        String enabled = filters.get("enabled");
        if (enabled != null && ("true".equalsIgnoreCase(enabled) || "false".equalsIgnoreCase(enabled))) {
            where.and(schedule.enabled.eq(Boolean.parseBoolean(enabled)));
        }

        return where;
    }

    private Page<BatchSchedule> fetchPage(QBatchSchedule schedule, BooleanBuilder where, Pageable pageable) {
        JPAQuery<BatchSchedule> query = queryFactory
                .selectFrom(schedule)
                .where(where);

        applySort(query, pageable == null ? Sort.unsorted() : pageable.getSort(), schedule);
        if (pageable != null && pageable.isPaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }
        List<BatchSchedule> content = query.fetch();

        Long total = queryFactory
                .select(schedule.id.count())
                .from(schedule)
                .where(where)
                .fetchOne();
        long totalCount = total == null ? 0L : total;
        Pageable resultPageable = pageable == null ? Pageable.unpaged() : pageable;
        return new PageImpl<>(content, resultPageable, totalCount);
    }

    private void applySort(JPAQuery<BatchSchedule> query, Sort sort, QBatchSchedule schedule) {
        if (sort == null || sort.isUnsorted()) {
            query.orderBy(schedule.id.asc());
            return;
        }

        for (Sort.Order order : sort) {
            boolean asc = order.isAscending();
            switch (order.getProperty()) {
                case "id" -> applyNumberSort(query, schedule.id, asc);
                case "batchJobId" -> applyNumberSort(query, schedule.batchJobId, asc);
                case "cronExpression" -> query.orderBy(asc ? schedule.cronExpression.asc() : schedule.cronExpression.desc());
                case "timezone" -> query.orderBy(asc ? schedule.timezone.asc() : schedule.timezone.desc());
                case "enabled" -> query.orderBy(asc ? schedule.enabled.asc() : schedule.enabled.desc());
                case "lastStatus" -> query.orderBy(asc ? schedule.lastStatus.asc() : schedule.lastStatus.desc());
                case "lastRunAt" -> query.orderBy(asc ? schedule.lastRunAt.asc() : schedule.lastRunAt.desc());
                default -> applyNumberSort(query, schedule.id, asc);
            }
        }
    }

    private <N extends Number & Comparable<?>> void applyNumberSort(
            JPAQuery<BatchSchedule> query,
            NumberPath<N> path,
            boolean ascending
    ) {
        query.orderBy(ascending ? path.asc() : path.desc());
    }
}
