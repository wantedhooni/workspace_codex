package com.revy.mvpbanking.transaction.domain;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class AdminTransactionQueryRepository {

    private final JPAQueryFactory queryFactory;

    public AdminTransactionQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Page<TransactionEntry> search(
            String query,
            TransactionStatus status,
            TransactionType type,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Instant occurredFrom,
            Instant occurredTo,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        QTransactionEntry transactionEntry = QTransactionEntry.transactionEntry;
        BooleanBuilder where = new BooleanBuilder();

        if (query != null && !query.isBlank()) {
            where.and(
                    transactionEntry.transactionNumber.containsIgnoreCase(query)
                            .or(transactionEntry.currency.containsIgnoreCase(query))
            );
        }

        if (status != null) {
            where.and(transactionEntry.status.eq(status));
        }

        if (type != null) {
            where.and(transactionEntry.transactionType.eq(type));
        }

        if (minAmount != null) {
            where.and(transactionEntry.amount.goe(minAmount));
        }

        if (maxAmount != null) {
            where.and(transactionEntry.amount.loe(maxAmount));
        }

        if (occurredFrom != null) {
            where.and(transactionEntry.occurredAt.goe(occurredFrom));
        }

        if (occurredTo != null) {
            where.and(transactionEntry.occurredAt.lt(occurredTo));
        }

        List<TransactionEntry> items = queryFactory
                .selectFrom(transactionEntry)
                .where(where)
                .orderBy(resolveOrder(transactionEntry, sortBy, sortDir))
                .offset((long) page * size)
                .limit(size)
                .fetch();

        Long total = queryFactory
                .select(transactionEntry.count())
                .from(transactionEntry)
                .where(where)
                .fetchOne();

        return new PageImpl<>(items, PageRequest.of(page, size), total == null ? 0 : total);
    }

    private OrderSpecifier<?> resolveOrder(QTransactionEntry transactionEntry, String sortBy, String sortDir) {
        Order direction = "asc".equalsIgnoreCase(sortDir) ? Order.ASC : Order.DESC;
        String normalizedSortBy = sortBy == null ? "occurredAt" : sortBy;

        return switch (normalizedSortBy) {
            case "transactionNumber" -> new OrderSpecifier<>(direction, transactionEntry.transactionNumber);
            case "amount" -> new OrderSpecifier<>(direction, transactionEntry.amount);
            default -> new OrderSpecifier<>(direction, transactionEntry.occurredAt);
        };
    }
}
