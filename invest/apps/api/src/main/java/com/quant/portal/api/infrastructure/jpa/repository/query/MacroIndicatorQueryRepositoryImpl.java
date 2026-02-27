package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quant.portal.api.application.query.MacroIndicatorSearchCondition;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslPredicateBuilder;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslSortMapper;
import com.quant.portal.domain.macro.entity.MacroIndicator;
import com.quant.portal.domain.macro.entity.QMacroIndicator;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
public class MacroIndicatorQueryRepositoryImpl implements MacroIndicatorQueryRepository {

    private final JPAQueryFactory queryFactory;

    public MacroIndicatorQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<MacroIndicator> search(MacroIndicatorSearchCondition condition, Pageable pageable) {
        QMacroIndicator indicator = QMacroIndicator.macroIndicator;

        BooleanBuilder where = new BooleanBuilder();
        where.and(QuerydslPredicateBuilder.eqIfPresent(indicator.regionCode, condition.regionCode()));
        where.and(QuerydslPredicateBuilder.betweenIfPresent(indicator.observedDate, condition.fromDate(), condition.toDate()));

        String keyword = condition.keyword();
        if (keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();
            BooleanBuilder keywordWhere = new BooleanBuilder();
            keywordWhere.or(indicator.indicatorCode.containsIgnoreCase(trimmedKeyword));
            keywordWhere.or(indicator.indicatorName.containsIgnoreCase(trimmedKeyword));
            where.and(keywordWhere);
        }

        List<MacroIndicator> content = queryFactory
                .selectFrom(indicator)
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QuerydslSortMapper.toOrderSpecifiers(pageable.getSort(), sortWhitelist(indicator)))
                .fetch();

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> {
                    Long count = queryFactory
                            .select(indicator.count())
                            .from(indicator)
                            .where(where)
                            .fetchOne();
                    return count == null ? 0L : count;
                }
        );
    }

    private Map<String, ComparableExpressionBase<?>> sortWhitelist(QMacroIndicator indicator) {
        return Map.of(
                "id", indicator.id,
                "indicatorCode", indicator.indicatorCode,
                "indicatorName", indicator.indicatorName,
                "regionCode", indicator.regionCode,
                "observedDate", indicator.observedDate,
                "indicatorValue", indicator.indicatorValue,
                "createdAt", indicator.createdAt
        );
    }
}
