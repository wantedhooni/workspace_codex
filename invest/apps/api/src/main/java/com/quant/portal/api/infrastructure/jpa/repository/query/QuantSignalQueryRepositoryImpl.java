package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quant.portal.api.application.query.QuantSignalSearchCondition;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslPredicateBuilder;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslSortMapper;
import com.quant.portal.domain.quant.entity.QQuantSignal;
import com.quant.portal.domain.quant.entity.QuantSignal;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
public class QuantSignalQueryRepositoryImpl implements QuantSignalQueryRepository {

    private final JPAQueryFactory queryFactory;

    public QuantSignalQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<QuantSignal> search(QuantSignalSearchCondition condition, Pageable pageable) {
        QQuantSignal signal = QQuantSignal.quantSignal;

        BooleanBuilder where = new BooleanBuilder();
        where.and(QuerydslPredicateBuilder.eqIfPresent(signal.strategy.id, condition.strategyId()));
        where.and(QuerydslPredicateBuilder.eqIfPresent(signal.instrument.id, condition.instrumentId()));
        where.and(QuerydslPredicateBuilder.eqIfPresent(signal.signalType, condition.signalType()));
        where.and(QuerydslPredicateBuilder.betweenIfPresent(signal.signalDate, condition.fromDate(), condition.toDate()));

        List<QuantSignal> content = queryFactory
                .selectFrom(signal)
                .leftJoin(signal.strategy).fetchJoin()
                .leftJoin(signal.instrument).fetchJoin()
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QuerydslSortMapper.toOrderSpecifiers(pageable.getSort(), sortWhitelist(signal)))
                .fetch();

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> {
                    Long count = queryFactory
                            .select(signal.count())
                            .from(signal)
                            .where(where)
                            .fetchOne();
                    return count == null ? 0L : count;
                }
        );
    }

    private Map<String, ComparableExpressionBase<?>> sortWhitelist(QQuantSignal signal) {
        return Map.of(
                "id", signal.id,
                "signalDate", signal.signalDate,
                "signalType", signal.signalType,
                "score", signal.score,
                "confidence", signal.confidence,
                "createdAt", signal.createdAt
        );
    }
}
