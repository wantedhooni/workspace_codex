package com.revy.scaffolding.order.domain.repository;

import static com.revy.scaffolding.order.domain.QPurchaseOrder.purchaseOrder;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.scaffolding.order.dto.OrderSummaryResponse;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PurchaseOrderQueryRepositoryImpl implements PurchaseOrderQueryRepository {
    private final JPAQueryFactory queryFactory;

    public PurchaseOrderQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<OrderSummaryResponse> findByUserId(Long userId) {
        return queryFactory
            .selectFrom(purchaseOrder)
            .where(eqUserId(userId))
            .orderBy(purchaseOrder.id.desc())
            .fetch()
            .stream()
            .map(OrderSummaryResponse::from)
            .toList();
    }

    private BooleanExpression eqUserId(Long userId) {
        return userId == null ? null : purchaseOrder.userId.eq(userId);
    }
}

