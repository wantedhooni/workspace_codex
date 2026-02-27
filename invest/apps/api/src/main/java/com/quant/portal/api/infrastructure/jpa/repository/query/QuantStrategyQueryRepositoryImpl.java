package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quant.portal.api.application.query.QuantStrategySearchCondition;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslPredicateBuilder;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslSortMapper;
import com.quant.portal.domain.quant.entity.QQuantStrategy;
import com.quant.portal.domain.quant.entity.QuantStrategy;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
public class QuantStrategyQueryRepositoryImpl implements QuantStrategyQueryRepository {

    private final JPAQueryFactory queryFactory;

    public QuantStrategyQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<QuantStrategy> search(QuantStrategySearchCondition condition, Pageable pageable) {
        QQuantStrategy strategy = QQuantStrategy.quantStrategy;

        BooleanBuilder where = new BooleanBuilder();
        where.and(QuerydslPredicateBuilder.eqIfPresent(strategy.style, condition.style()));
        where.and(QuerydslPredicateBuilder.eqIfPresent(strategy.status, condition.status()));

        String keyword = condition.keyword();
        if (keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();
            BooleanBuilder keywordWhere = new BooleanBuilder();
            keywordWhere.or(strategy.name.containsIgnoreCase(trimmedKeyword));
            keywordWhere.or(strategy.description.containsIgnoreCase(trimmedKeyword));
            where.and(keywordWhere);
        }

        List<QuantStrategy> content = queryFactory
                .selectFrom(strategy)
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QuerydslSortMapper.toOrderSpecifiers(pageable.getSort(), sortWhitelist(strategy)))
                .fetch();

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> {
                    Long count = queryFactory
                            .select(strategy.count())
                            .from(strategy)
                            .where(where)
                            .fetchOne();
                    return count == null ? 0L : count;
                }
        );
    }

    private Map<String, ComparableExpressionBase<?>> sortWhitelist(QQuantStrategy strategy) {
        return Map.of(
                "id", strategy.id,
                "name", strategy.name,
                "style", strategy.style,
                "status", strategy.status,
                "rebalanceCycleDays", strategy.rebalanceCycleDays,
                "createdAt", strategy.createdAt
        );
    }
}
