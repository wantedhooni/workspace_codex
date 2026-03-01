package com.derivops.mvp.opscase.infrastructure;
import com.derivops.mvp.opscase.*;
import com.derivops.mvp.opscase.api.*;
import com.derivops.mvp.opscase.application.*;
import com.derivops.mvp.opscase.dto.*;


import com.derivops.mvp.common.rsql.QuerydslRsqlPredicateBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class OpsCaseRepositoryImpl implements OpsCaseRepositoryCustom {

    private static final QOpsCase opsCase = QOpsCase.opsCase;

    private static final Map<String, Expression<?>> FILTER_FIELDS = Map.ofEntries(
            Map.entry("id", opsCase.id),
            Map.entry("caseNo", opsCase.caseNo),
            Map.entry("category", opsCase.category),
            Map.entry("severity", opsCase.severity),
            Map.entry("status", opsCase.status),
            Map.entry("title", opsCase.title),
            Map.entry("description", opsCase.description),
            Map.entry("assignee", opsCase.assignee),
            Map.entry("dueAt", opsCase.dueAt),
            Map.entry("linkedType", opsCase.linkedType),
            Map.entry("linkedId", opsCase.linkedId),
            Map.entry("accountId", opsCase.accountId),
            Map.entry("resolutionSummary", opsCase.resolutionSummary),
            Map.entry("createdBy", opsCase.createdBy),
            Map.entry("updatedBy", opsCase.updatedBy),
            Map.entry("resolvedBy", opsCase.resolvedBy),
            Map.entry("resolvedAt", opsCase.resolvedAt),
            Map.entry("closedBy", opsCase.closedBy),
            Map.entry("closedAt", opsCase.closedAt),
            Map.entry("createdAt", opsCase.createdAt),
            Map.entry("updatedAt", opsCase.updatedAt)
    );

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<OpsCase> search(
            OpsCaseStatus status,
            OpsCaseSeverity severity,
            String assignee,
            String keyword,
            String filter,
            Pageable pageable
    ) {
        BooleanBuilder builder = new BooleanBuilder();
        if (status != null) {
            builder.and(opsCase.status.eq(status));
        }
        if (severity != null) {
            builder.and(opsCase.severity.eq(severity));
        }
        if (assignee != null && !assignee.isBlank()) {
            builder.and(opsCase.assignee.equalsIgnoreCase(assignee.trim()));
        }
        if (keyword != null && !keyword.isBlank()) {
            String q = keyword.trim();
            builder.and(
                    opsCase.caseNo.containsIgnoreCase(q)
                            .or(opsCase.title.containsIgnoreCase(q))
                            .or(opsCase.description.containsIgnoreCase(q))
                            .or(opsCase.linkedType.containsIgnoreCase(q))
                            .or(opsCase.linkedId.containsIgnoreCase(q))
                            .or(opsCase.assignee.containsIgnoreCase(q))
                            .or(opsCase.createdBy.containsIgnoreCase(q))
                            .or(opsCase.resolutionSummary.containsIgnoreCase(q))
            );
        }
        if (filter != null && !filter.isBlank()) {
            builder.and(QuerydslRsqlPredicateBuilder.build(filter, FILTER_FIELDS));
        }

        List<OpsCase> items = queryFactory
                .selectFrom(opsCase)
                .where(builder)
                .orderBy(opsCase.createdAt.desc(), opsCase.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(opsCase.count())
                .from(opsCase)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(items, pageable, total == null ? 0L : total);
    }
}
