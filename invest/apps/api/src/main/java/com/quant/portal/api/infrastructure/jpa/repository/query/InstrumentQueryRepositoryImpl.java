package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quant.portal.api.application.query.InstrumentSearchCondition;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslPredicateBuilder;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslSortMapper;
import com.quant.portal.domain.portfolio.entity.Instrument;
import com.quant.portal.domain.portfolio.entity.QInstrument;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
public class InstrumentQueryRepositoryImpl implements InstrumentQueryRepository {

    private final JPAQueryFactory queryFactory;

    public InstrumentQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<Instrument> search(InstrumentSearchCondition condition, Pageable pageable) {
        QInstrument instrument = QInstrument.instrument;

        BooleanBuilder where = new BooleanBuilder();
        where.and(QuerydslPredicateBuilder.eqIfPresent(instrument.marketCode, condition.marketCode()));
        where.and(QuerydslPredicateBuilder.eqIfPresent(instrument.currencyCode, condition.currencyCode()));

        if (condition.keyword() != null && !condition.keyword().isBlank()) {
            where.and(
                    QuerydslPredicateBuilder.likeIfPresent(instrument.ticker, condition.keyword())
                            .or(QuerydslPredicateBuilder.likeIfPresent(instrument.name, condition.keyword()))
            );
        }

        List<Instrument> content = queryFactory
                .selectFrom(instrument)
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QuerydslSortMapper.toOrderSpecifiers(pageable.getSort(), sortWhitelist(instrument)))
                .fetch();

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> {
                    Long count = queryFactory
                            .select(instrument.count())
                            .from(instrument)
                            .where(where)
                            .fetchOne();
                    return count == null ? 0L : count;
                }
        );
    }

    private Map<String, ComparableExpressionBase<?>> sortWhitelist(QInstrument instrument) {
        return Map.of(
                "id", instrument.id,
                "ticker", instrument.ticker,
                "name", instrument.name,
                "marketCode", instrument.marketCode,
                "createdAt", instrument.createdAt
        );
    }
}
