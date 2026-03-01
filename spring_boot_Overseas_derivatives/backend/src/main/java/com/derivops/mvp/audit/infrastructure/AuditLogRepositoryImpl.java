package com.derivops.mvp.audit.infrastructure;
import com.derivops.mvp.audit.*;
import com.derivops.mvp.audit.api.*;
import com.derivops.mvp.audit.application.*;
import com.derivops.mvp.audit.dto.*;


import com.derivops.mvp.common.rsql.QuerydslRsqlPredicateBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepositoryCustom {

    private static final QAuditLog auditLog = QAuditLog.auditLog;

    private static final Map<String, Expression<?>> FILTER_FIELDS = Map.ofEntries(
            Map.entry("id", auditLog.id),
            Map.entry("actor", auditLog.actor),
            Map.entry("action", auditLog.action),
            Map.entry("targetType", auditLog.targetType),
            Map.entry("targetId", auditLog.targetId),
            Map.entry("details", auditLog.details),
            Map.entry("createdAt", auditLog.createdAt)
    );

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AuditLog> search(
            String actor,
            String action,
            String keyword,
            String filter,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    ) {
        BooleanBuilder builder = new BooleanBuilder();
        if (actor != null && !actor.isBlank()) {
            builder.and(auditLog.actor.eq(actor));
        }
        if (action != null && !action.isBlank()) {
            builder.and(auditLog.action.eq(action));
        }
        if (keyword != null && !keyword.isBlank()) {
            String q = keyword.trim();
            builder.and(
                    auditLog.actor.containsIgnoreCase(q)
                            .or(auditLog.action.containsIgnoreCase(q))
                            .or(auditLog.targetType.containsIgnoreCase(q))
                            .or(auditLog.targetId.containsIgnoreCase(q))
                            .or(auditLog.details.containsIgnoreCase(q))
            );
        }
        if (from != null) {
            builder.and(auditLog.createdAt.goe(from));
        }
        if (to != null) {
            builder.and(auditLog.createdAt.loe(to));
        }
        if (filter != null && !filter.isBlank()) {
            builder.and(QuerydslRsqlPredicateBuilder.build(filter, FILTER_FIELDS));
        }

        List<AuditLog> items = queryFactory
                .selectFrom(auditLog)
                .where(builder)
                .orderBy(auditLog.createdAt.desc(), auditLog.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(auditLog.count())
                .from(auditLog)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(items, pageable, total == null ? 0L : total);
    }
}
