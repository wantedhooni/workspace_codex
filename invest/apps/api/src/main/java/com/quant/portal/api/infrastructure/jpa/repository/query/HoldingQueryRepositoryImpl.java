package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quant.portal.api.application.query.HoldingSearchCondition;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslPredicateBuilder;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslSortMapper;
import com.quant.portal.domain.portfolio.entity.Holding;
import com.quant.portal.domain.portfolio.entity.QHolding;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
public class HoldingQueryRepositoryImpl implements HoldingQueryRepository {

    private final JPAQueryFactory queryFactory;

    public HoldingQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<Holding> search(HoldingSearchCondition condition, Pageable pageable) {
        QHolding holding = QHolding.holding;

        BooleanBuilder where = new BooleanBuilder();
        where.and(QuerydslPredicateBuilder.eqIfPresent(holding.portfolio.id, condition.portfolioId()));
        where.and(QuerydslPredicateBuilder.eqIfPresent(holding.instrument.id, condition.instrumentId()));

        if (condition.keyword() != null && !condition.keyword().isBlank()) {
            where.and(
                    QuerydslPredicateBuilder.likeIfPresent(holding.instrument.ticker, condition.keyword())
                            .or(QuerydslPredicateBuilder.likeIfPresent(holding.instrument.name, condition.keyword()))
            );
        }

        List<Holding> content = queryFactory
                .selectFrom(holding)
                .leftJoin(holding.portfolio).fetchJoin()
                .leftJoin(holding.instrument).fetchJoin()
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QuerydslSortMapper.toOrderSpecifiers(pageable.getSort(), sortWhitelist(holding)))
                .fetch();

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> {
                    Long count = queryFactory
                            .select(holding.count())
                            .from(holding)
                            .where(where)
                            .fetchOne();
                    return count == null ? 0L : count;
                }
        );
    }

    private Map<String, ComparableExpressionBase<?>> sortWhitelist(QHolding holding) {
        return Map.of(
                "id", holding.id,
                "ticker", holding.instrument.ticker,
                "quantity", holding.quantity,
                "averageCost", holding.averageCost,
                "createdAt", holding.createdAt
        );
    }
}
