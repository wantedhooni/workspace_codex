package com.portal.admin.repo;

import com.portal.admin.domain.BatchJob;
import com.portal.admin.domain.QBatchJob;
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

public class BatchJobRepositoryImpl implements SearchableRepository<BatchJob>, FieldSearchableRepository<BatchJob> {

    private final JPAQueryFactory queryFactory;

    public BatchJobRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<BatchJob> search(String query) {
        QBatchJob batchJob = QBatchJob.batchJob;
        return queryFactory
                .selectFrom(batchJob)
                .where(
                        batchJob.name.containsIgnoreCase(query)
                                .or(batchJob.jobKey.containsIgnoreCase(query))
                                .or(batchJob.description.containsIgnoreCase(query))
                )
                .orderBy(batchJob.id.asc())
                .fetch();
    }

    @Override
    public Page<BatchJob> search(String query, Pageable pageable) {
        QBatchJob batchJob = QBatchJob.batchJob;
        BooleanBuilder where = new BooleanBuilder()
                .and(
                        batchJob.name.containsIgnoreCase(query)
                                .or(batchJob.jobKey.containsIgnoreCase(query))
                                .or(batchJob.description.containsIgnoreCase(query))
                );
        return fetchPage(batchJob, where, pageable);
    }

    @Override
    public List<BatchJob> searchByFields(Map<String, String> filters) {
        QBatchJob batchJob = QBatchJob.batchJob;
        BooleanBuilder where = buildFieldWhere(batchJob, filters);

        return queryFactory
                .selectFrom(batchJob)
                .where(where)
                .orderBy(batchJob.id.asc())
                .fetch();
    }

    @Override
    public Page<BatchJob> searchByFields(Map<String, String> filters, Pageable pageable) {
        QBatchJob batchJob = QBatchJob.batchJob;
        BooleanBuilder where = buildFieldWhere(batchJob, filters);
        return fetchPage(batchJob, where, pageable);
    }

    @Override
    public Set<String> allowedSortFields() {
        return Set.of("id", "name", "jobKey", "description", "enabled");
    }

    @Override
    public Set<String> allowedFilterFields() {
        return Set.of("name", "jobKey", "description", "enabled");
    }

    private BooleanBuilder buildFieldWhere(QBatchJob batchJob, Map<String, String> filters) {
        BooleanBuilder where = new BooleanBuilder();

        String name = filters.get("name");
        if (name != null && !name.isBlank()) {
            where.and(batchJob.name.containsIgnoreCase(name.trim()));
        }

        String jobKey = filters.get("jobKey");
        if (jobKey != null && !jobKey.isBlank()) {
            where.and(batchJob.jobKey.containsIgnoreCase(jobKey.trim()));
        }

        String description = filters.get("description");
        if (description != null && !description.isBlank()) {
            where.and(batchJob.description.containsIgnoreCase(description.trim()));
        }

        String enabled = filters.get("enabled");
        if (enabled != null && ("true".equalsIgnoreCase(enabled) || "false".equalsIgnoreCase(enabled))) {
            where.and(batchJob.enabled.eq(Boolean.parseBoolean(enabled)));
        }

        return where;
    }

    private Page<BatchJob> fetchPage(QBatchJob batchJob, BooleanBuilder where, Pageable pageable) {
        JPAQuery<BatchJob> query = queryFactory
                .selectFrom(batchJob)
                .where(where);

        applySort(query, pageable == null ? Sort.unsorted() : pageable.getSort(), batchJob);
        if (pageable != null && pageable.isPaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }
        List<BatchJob> content = query.fetch();

        Long total = queryFactory
                .select(batchJob.id.count())
                .from(batchJob)
                .where(where)
                .fetchOne();
        long totalCount = total == null ? 0L : total;
        Pageable resultPageable = pageable == null ? Pageable.unpaged() : pageable;
        return new PageImpl<>(content, resultPageable, totalCount);
    }

    private void applySort(JPAQuery<BatchJob> query, Sort sort, QBatchJob batchJob) {
        if (sort == null || sort.isUnsorted()) {
            query.orderBy(batchJob.id.asc());
            return;
        }

        for (Sort.Order order : sort) {
            boolean asc = order.isAscending();
            switch (order.getProperty()) {
                case "id" -> applyNumberSort(query, batchJob.id, asc);
                case "name" -> query.orderBy(asc ? batchJob.name.asc() : batchJob.name.desc());
                case "jobKey" -> query.orderBy(asc ? batchJob.jobKey.asc() : batchJob.jobKey.desc());
                case "description" -> query.orderBy(asc ? batchJob.description.asc() : batchJob.description.desc());
                case "enabled" -> query.orderBy(asc ? batchJob.enabled.asc() : batchJob.enabled.desc());
                default -> applyNumberSort(query, batchJob.id, asc);
            }
        }
    }

    private <N extends Number & Comparable<?>> void applyNumberSort(
            JPAQuery<BatchJob> query,
            NumberPath<N> path,
            boolean ascending
    ) {
        query.orderBy(ascending ? path.asc() : path.desc());
    }
}
