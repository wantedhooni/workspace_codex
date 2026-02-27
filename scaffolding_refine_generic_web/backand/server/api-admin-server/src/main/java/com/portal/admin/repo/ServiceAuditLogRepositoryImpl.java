package com.portal.admin.repo;

import com.portal.admin.domain.QServiceAuditLog;
import com.portal.admin.domain.ServiceAuditLog;
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

public class ServiceAuditLogRepositoryImpl implements SearchableRepository<ServiceAuditLog>, FieldSearchableRepository<ServiceAuditLog> {

    private final JPAQueryFactory queryFactory;

    public ServiceAuditLogRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<ServiceAuditLog> search(String query) {
        QServiceAuditLog auditLog = QServiceAuditLog.serviceAuditLog;
        return queryFactory
                .selectFrom(auditLog)
                .where(
                        auditLog.domainType.containsIgnoreCase(query)
                                .or(auditLog.action.containsIgnoreCase(query))
                                .or(auditLog.username.containsIgnoreCase(query))
                                .or(auditLog.detail.containsIgnoreCase(query))
                )
                .orderBy(auditLog.id.desc())
                .fetch();
    }

    @Override
    public Page<ServiceAuditLog> search(String query, Pageable pageable) {
        QServiceAuditLog auditLog = QServiceAuditLog.serviceAuditLog;
        BooleanBuilder where = new BooleanBuilder()
                .and(
                        auditLog.domainType.containsIgnoreCase(query)
                                .or(auditLog.action.containsIgnoreCase(query))
                                .or(auditLog.username.containsIgnoreCase(query))
                                .or(auditLog.detail.containsIgnoreCase(query))
                );
        return fetchPage(auditLog, where, pageable);
    }

    @Override
    public List<ServiceAuditLog> searchByFields(Map<String, String> filters) {
        QServiceAuditLog auditLog = QServiceAuditLog.serviceAuditLog;
        BooleanBuilder where = buildFieldWhere(auditLog, filters);

        return queryFactory
                .selectFrom(auditLog)
                .where(where)
                .orderBy(auditLog.id.desc())
                .fetch();
    }

    @Override
    public Page<ServiceAuditLog> searchByFields(Map<String, String> filters, Pageable pageable) {
        QServiceAuditLog auditLog = QServiceAuditLog.serviceAuditLog;
        BooleanBuilder where = buildFieldWhere(auditLog, filters);
        return fetchPage(auditLog, where, pageable);
    }

    @Override
    public Set<String> allowedSortFields() {
        return Set.of("id", "domainType", "domainId", "action", "username", "loggedAt");
    }

    @Override
    public Set<String> allowedFilterFields() {
        return Set.of("domainType", "domainId", "action", "username", "detail");
    }

    private BooleanBuilder buildFieldWhere(QServiceAuditLog auditLog, Map<String, String> filters) {
        BooleanBuilder where = new BooleanBuilder();

        String domainType = filters.get("domainType");
        if (domainType != null && !domainType.isBlank()) {
            where.and(auditLog.domainType.containsIgnoreCase(domainType.trim()));
        }

        String domainId = filters.get("domainId");
        if (domainId != null) {
            try {
                where.and(auditLog.domainId.eq(Long.parseLong(domainId.trim())));
            } catch (NumberFormatException ignored) {
            }
        }

        String action = filters.get("action");
        if (action != null && !action.isBlank()) {
            where.and(auditLog.action.containsIgnoreCase(action.trim()));
        }

        String username = filters.get("username");
        if (username != null && !username.isBlank()) {
            where.and(auditLog.username.containsIgnoreCase(username.trim()));
        }

        String detail = filters.get("detail");
        if (detail != null && !detail.isBlank()) {
            where.and(auditLog.detail.containsIgnoreCase(detail.trim()));
        }

        return where;
    }

    private Page<ServiceAuditLog> fetchPage(QServiceAuditLog auditLog, BooleanBuilder where, Pageable pageable) {
        JPAQuery<ServiceAuditLog> query = queryFactory
                .selectFrom(auditLog)
                .where(where);

        applySort(query, pageable == null ? Sort.unsorted() : pageable.getSort(), auditLog);
        if (pageable != null && pageable.isPaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }
        List<ServiceAuditLog> content = query.fetch();

        Long total = queryFactory
                .select(auditLog.id.count())
                .from(auditLog)
                .where(where)
                .fetchOne();
        long totalCount = total == null ? 0L : total;
        Pageable resultPageable = pageable == null ? Pageable.unpaged() : pageable;
        return new PageImpl<>(content, resultPageable, totalCount);
    }

    private void applySort(JPAQuery<ServiceAuditLog> query, Sort sort, QServiceAuditLog auditLog) {
        if (sort == null || sort.isUnsorted()) {
            query.orderBy(auditLog.id.desc());
            return;
        }

        for (Sort.Order order : sort) {
            boolean asc = order.isAscending();
            switch (order.getProperty()) {
                case "id" -> applyNumberSort(query, auditLog.id, asc);
                case "domainType" -> query.orderBy(asc ? auditLog.domainType.asc() : auditLog.domainType.desc());
                case "domainId" -> applyNumberSort(query, auditLog.domainId, asc);
                case "action" -> query.orderBy(asc ? auditLog.action.asc() : auditLog.action.desc());
                case "username" -> query.orderBy(asc ? auditLog.username.asc() : auditLog.username.desc());
                case "loggedAt" -> query.orderBy(asc ? auditLog.loggedAt.asc() : auditLog.loggedAt.desc());
                default -> applyNumberSort(query, auditLog.id, asc);
            }
        }
    }

    private <N extends Number & Comparable<?>> void applyNumberSort(
            JPAQuery<ServiceAuditLog> query,
            NumberPath<N> path,
            boolean ascending
    ) {
        query.orderBy(ascending ? path.asc() : path.desc());
    }
}
