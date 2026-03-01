package com.derivops.mvp.risk.infrastructure;
import com.derivops.mvp.risk.*;
import com.derivops.mvp.risk.api.*;
import com.derivops.mvp.risk.application.*;
import com.derivops.mvp.risk.dto.*;


import com.derivops.mvp.approval.ApprovalDomain;
import com.derivops.mvp.common.rsql.QuerydslRsqlPredicateBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class RiskLimitPolicyRepositoryImpl implements RiskLimitPolicyRepositoryCustom {

    private static final QRiskLimitPolicy policy = QRiskLimitPolicy.riskLimitPolicy;

    private static final Map<String, Expression<?>> FILTER_FIELDS = Map.ofEntries(
            Map.entry("id", policy.id),
            Map.entry("brokerCode", policy.brokerCode),
            Map.entry("domain", policy.domain),
            Map.entry("currencyCode", policy.currencyCode),
            Map.entry("maxPerRequest", policy.maxPerRequest),
            Map.entry("dailySoftLimit", policy.dailySoftLimit),
            Map.entry("dailyHardLimit", policy.dailyHardLimit),
            Map.entry("enabled", policy.enabled),
            Map.entry("effectiveFrom", policy.effectiveFrom),
            Map.entry("effectiveTo", policy.effectiveTo),
            Map.entry("description", policy.description),
            Map.entry("createdAt", policy.createdAt)
    );

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<RiskLimitPolicy> search(String keyword, String filter, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        if (keyword != null && !keyword.isBlank()) {
            String q = keyword.trim();
            builder.and(
                    policy.brokerCode.containsIgnoreCase(q)
                            .or(policy.domain.stringValue().containsIgnoreCase(q))
                            .or(policy.currencyCode.containsIgnoreCase(q))
                            .or(policy.description.containsIgnoreCase(q))
            );
        }
        if (filter != null && !filter.isBlank()) {
            builder.and(QuerydslRsqlPredicateBuilder.build(filter, FILTER_FIELDS));
        }

        List<RiskLimitPolicy> items = queryFactory
                .selectFrom(policy)
                .where(builder)
                .orderBy(policy.brokerCode.asc(), policy.domain.asc(), policy.currencyCode.asc().nullsLast(), policy.effectiveFrom.desc(), policy.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(policy.count())
                .from(policy)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(items, pageable, total == null ? 0L : total);
    }

    @Override
    public Optional<RiskLimitPolicy> resolve(String brokerCode, ApprovalDomain domain, String currencyCode, LocalDate targetDate) {
        if (currencyCode != null && !currencyCode.isBlank()) {
            RiskLimitPolicy byCurrency = baseResolveQuery(brokerCode, domain, targetDate)
                    .where(policy.currencyCode.eq(currencyCode))
                    .fetchFirst();
            if (byCurrency != null) {
                return Optional.of(byCurrency);
            }
        }

        RiskLimitPolicy byDomain = baseResolveQuery(brokerCode, domain, targetDate)
                .where(policy.currencyCode.isNull())
                .fetchFirst();
        return Optional.ofNullable(byDomain);
    }

    private com.querydsl.jpa.impl.JPAQuery<RiskLimitPolicy> baseResolveQuery(
            String brokerCode,
            ApprovalDomain domain,
            LocalDate targetDate
    ) {
        return queryFactory
                .selectFrom(policy)
                .where(
                        policy.brokerCode.eq(brokerCode),
                        policy.domain.eq(domain),
                        policy.enabled.isTrue(),
                        policy.effectiveFrom.loe(targetDate),
                        policy.effectiveTo.isNull().or(policy.effectiveTo.goe(targetDate))
                )
                .orderBy(policy.effectiveFrom.desc(), policy.id.desc());
    }
}
