package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quant.portal.api.application.query.PortfolioSearchCondition;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslPredicateBuilder;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslSortMapper;
import com.quant.portal.domain.portfolio.entity.Portfolio;
import com.quant.portal.domain.portfolio.entity.QPortfolio;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
public class PortfolioQueryRepositoryImpl implements PortfolioQueryRepository {

    private final JPAQueryFactory queryFactory;

    public PortfolioQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<Portfolio> search(PortfolioSearchCondition condition, Pageable pageable) {
        QPortfolio portfolio = QPortfolio.portfolio;

        BooleanBuilder where = new BooleanBuilder();
        where.and(QuerydslPredicateBuilder.eqIfPresent(portfolio.baseCurrency, condition.baseCurrency()));
        where.and(QuerydslPredicateBuilder.likeIfPresent(portfolio.name, condition.keyword()));

        List<Portfolio> content = queryFactory
                .selectFrom(portfolio)
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QuerydslSortMapper.toOrderSpecifiers(pageable.getSort(), sortWhitelist(portfolio)))
                .fetch();

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> {
                    Long count = queryFactory
                            .select(portfolio.count())
                            .from(portfolio)
                            .where(where)
                            .fetchOne();
                    return count == null ? 0L : count;
                }
        );
    }

    private Map<String, ComparableExpressionBase<?>> sortWhitelist(QPortfolio portfolio) {
        return Map.of(
                "id", portfolio.id,
                "name", portfolio.name,
                "baseCurrency", portfolio.baseCurrency,
                "cashBalance", portfolio.cashBalance,
                "createdAt", portfolio.createdAt
        );
    }
}
