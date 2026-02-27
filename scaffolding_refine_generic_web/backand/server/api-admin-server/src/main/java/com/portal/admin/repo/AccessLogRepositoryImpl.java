package com.portal.admin.repo;

import com.portal.admin.domain.AccessLog;
import com.portal.admin.domain.QAccessLog;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AccessLogRepositoryImpl implements SearchableRepository<AccessLog>, FieldSearchableRepository<AccessLog>, AccessLogStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    public AccessLogRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<AccessLog> search(String query) {
        QAccessLog accessLog = QAccessLog.accessLog;
        return queryFactory
                .selectFrom(accessLog)
                .where(
                        accessLog.username.containsIgnoreCase(query)
                                .or(accessLog.ipAddress.containsIgnoreCase(query))
                                .or(accessLog.action.containsIgnoreCase(query))
                                .or(accessLog.path.containsIgnoreCase(query))
                )
                .orderBy(accessLog.id.desc())
                .fetch();
    }

    @Override
    public Page<AccessLog> search(String query, Pageable pageable) {
        QAccessLog accessLog = QAccessLog.accessLog;
        BooleanBuilder where = new BooleanBuilder()
                .and(
                        accessLog.username.containsIgnoreCase(query)
                                .or(accessLog.ipAddress.containsIgnoreCase(query))
                                .or(accessLog.action.containsIgnoreCase(query))
                                .or(accessLog.path.containsIgnoreCase(query))
                );
        return fetchPage(accessLog, where, pageable);
    }

    @Override
    public List<AccessLog> searchByFields(Map<String, String> filters) {
        QAccessLog accessLog = QAccessLog.accessLog;
        BooleanBuilder where = buildFieldWhere(accessLog, filters);

        return queryFactory
                .selectFrom(accessLog)
                .where(where)
                .orderBy(accessLog.id.desc())
                .fetch();
    }

    @Override
    public Page<AccessLog> searchByFields(Map<String, String> filters, Pageable pageable) {
        QAccessLog accessLog = QAccessLog.accessLog;
        BooleanBuilder where = buildFieldWhere(accessLog, filters);
        return fetchPage(accessLog, where, pageable);
    }

    @Override
    public Set<String> allowedSortFields() {
        return Set.of("id", "username", "ipAddress", "action", "path", "success", "loggedAt");
    }

    @Override
    public Set<String> allowedFilterFields() {
        return Set.of("username", "ipAddress", "action", "path", "success");
    }

    @Override
    public long countSince(Instant from) {
        QAccessLog accessLog = QAccessLog.accessLog;
        Long count = queryFactory
                .select(accessLog.id.count())
                .from(accessLog)
                .where(accessLog.loggedAt.goe(from))
                .fetchOne();
        return count == null ? 0L : count;
    }

    @Override
    public long countSuccessfulActionSince(Instant from, String action) {
        QAccessLog accessLog = QAccessLog.accessLog;
        Long count = queryFactory
                .select(accessLog.id.count())
                .from(accessLog)
                .where(
                        accessLog.loggedAt.goe(from)
                                .and(accessLog.action.eq(action))
                                .and(accessLog.success.isTrue())
                )
                .fetchOne();
        return count == null ? 0L : count;
    }

    @Override
    public List<PathCountResult> findTopPathCounts(int limit) {
        QAccessLog accessLog = QAccessLog.accessLog;
        NumberExpression<Long> totalExpr = accessLog.id.count();
        List<Tuple> rows = queryFactory
                .select(accessLog.path, totalExpr)
                .from(accessLog)
                .groupBy(accessLog.path)
                .orderBy(totalExpr.desc())
                .limit(Math.max(1, limit))
                .fetch();

        return rows.stream()
                .map(row -> new PathCountResult(row.get(accessLog.path), valueOrZero(row.get(totalExpr))))
                .toList();
    }

    @Override
    public List<ActionCountResult> findTopActionCounts(int limit) {
        QAccessLog accessLog = QAccessLog.accessLog;
        NumberExpression<Long> totalExpr = accessLog.id.count();
        List<Tuple> rows = queryFactory
                .select(accessLog.action, totalExpr)
                .from(accessLog)
                .groupBy(accessLog.action)
                .orderBy(totalExpr.desc())
                .limit(Math.max(1, limit))
                .fetch();

        return rows.stream()
                .map(row -> new ActionCountResult(row.get(accessLog.action), valueOrZero(row.get(totalExpr))))
                .toList();
    }

    private BooleanBuilder buildFieldWhere(QAccessLog accessLog, Map<String, String> filters) {
        BooleanBuilder where = new BooleanBuilder();

        String username = filters.get("username");
        if (username != null && !username.isBlank()) {
            where.and(accessLog.username.containsIgnoreCase(username.trim()));
        }

        String ipAddress = filters.get("ipAddress");
        if (ipAddress != null && !ipAddress.isBlank()) {
            where.and(accessLog.ipAddress.containsIgnoreCase(ipAddress.trim()));
        }

        String action = filters.get("action");
        if (action != null && !action.isBlank()) {
            where.and(accessLog.action.containsIgnoreCase(action.trim()));
        }

        String path = filters.get("path");
        if (path != null && !path.isBlank()) {
            where.and(accessLog.path.containsIgnoreCase(path.trim()));
        }

        String success = filters.get("success");
        if (success != null && ("true".equalsIgnoreCase(success) || "false".equalsIgnoreCase(success))) {
            where.and(accessLog.success.eq(Boolean.parseBoolean(success)));
        }

        return where;
    }

    private Page<AccessLog> fetchPage(QAccessLog accessLog, BooleanBuilder where, Pageable pageable) {
        JPAQuery<AccessLog> query = queryFactory
                .selectFrom(accessLog)
                .where(where);

        applySort(query, pageable == null ? Sort.unsorted() : pageable.getSort(), accessLog);
        if (pageable != null && pageable.isPaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }
        List<AccessLog> content = query.fetch();

        Long total = queryFactory
                .select(accessLog.id.count())
                .from(accessLog)
                .where(where)
                .fetchOne();
        long totalCount = total == null ? 0L : total;
        Pageable resultPageable = pageable == null ? Pageable.unpaged() : pageable;
        return new PageImpl<>(content, resultPageable, totalCount);
    }

    private void applySort(JPAQuery<AccessLog> query, Sort sort, QAccessLog accessLog) {
        if (sort == null || sort.isUnsorted()) {
            query.orderBy(accessLog.id.desc());
            return;
        }

        for (Sort.Order order : sort) {
            boolean asc = order.isAscending();
            switch (order.getProperty()) {
                case "id" -> applyNumberSort(query, accessLog.id, asc);
                case "username" -> query.orderBy(asc ? accessLog.username.asc() : accessLog.username.desc());
                case "ipAddress" -> query.orderBy(asc ? accessLog.ipAddress.asc() : accessLog.ipAddress.desc());
                case "action" -> query.orderBy(asc ? accessLog.action.asc() : accessLog.action.desc());
                case "path" -> query.orderBy(asc ? accessLog.path.asc() : accessLog.path.desc());
                case "success" -> query.orderBy(asc ? accessLog.success.asc() : accessLog.success.desc());
                case "loggedAt" -> query.orderBy(asc ? accessLog.loggedAt.asc() : accessLog.loggedAt.desc());
                default -> applyNumberSort(query, accessLog.id, asc);
            }
        }
    }

    private <N extends Number & Comparable<?>> void applyNumberSort(
            JPAQuery<AccessLog> query,
            NumberPath<N> path,
            boolean ascending
    ) {
        query.orderBy(ascending ? path.asc() : path.desc());
    }

    private long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }
}
