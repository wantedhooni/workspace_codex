package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quant.portal.api.application.query.TransactionSearchCondition;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslPredicateBuilder;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslSortMapper;
import com.quant.portal.domain.portfolio.entity.PortfolioTransaction;
import com.quant.portal.domain.portfolio.entity.QPortfolioTransaction;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
public class PortfolioTransactionQueryRepositoryImpl implements PortfolioTransactionQueryRepository {

    private final JPAQueryFactory queryFactory;

    public PortfolioTransactionQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<PortfolioTransaction> search(TransactionSearchCondition condition, Pageable pageable) {
        QPortfolioTransaction transaction = QPortfolioTransaction.portfolioTransaction;

        BooleanBuilder where = new BooleanBuilder();
        where.and(QuerydslPredicateBuilder.eqIfPresent(transaction.portfolio.id, condition.portfolioId()));
        where.and(QuerydslPredicateBuilder.eqIfPresent(transaction.instrument.id, condition.instrumentId()));
        where.and(QuerydslPredicateBuilder.eqIfPresent(transaction.transactionType, condition.transactionType()));
        where.and(QuerydslPredicateBuilder.betweenIfPresent(transaction.tradeDate, condition.fromDate(), condition.toDate()));

        List<PortfolioTransaction> content = queryFactory
                .selectFrom(transaction)
                .leftJoin(transaction.portfolio).fetchJoin()
                .leftJoin(transaction.instrument).fetchJoin()
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QuerydslSortMapper.toOrderSpecifiers(pageable.getSort(), sortWhitelist(transaction)))
                .fetch();

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> {
                    Long count = queryFactory
                            .select(transaction.count())
                            .from(transaction)
                            .where(where)
                            .fetchOne();
                    return count == null ? 0L : count;
                }
        );
    }

    private Map<String, ComparableExpressionBase<?>> sortWhitelist(QPortfolioTransaction transaction) {
        return Map.of(
                "id", transaction.id,
                "tradeDate", transaction.tradeDate,
                "createdAt", transaction.createdAt,
                "amount", transaction.amount
        );
    }
}
