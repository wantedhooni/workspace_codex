package com.revy.mvpbanking.account.domain;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class AdminAccountQueryRepository {

    private final JPAQueryFactory queryFactory;

    public AdminAccountQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Page<Account> search(
            String query,
            AccountStatus status,
            AccountType accountType,
            BigDecimal minBalance,
            BigDecimal maxBalance,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        QAccount account = QAccount.account;
        BooleanBuilder where = new BooleanBuilder();

        if (query != null && !query.isBlank()) {
            where.and(
                    account.accountNumber.containsIgnoreCase(query)
                            .or(account.currency.containsIgnoreCase(query))
            );
        }

        if (status != null) {
            where.and(account.status.eq(status));
        }

        if (accountType != null) {
            where.and(account.accountType.eq(accountType));
        }

        if (minBalance != null) {
            where.and(account.balance.goe(minBalance));
        }

        if (maxBalance != null) {
            where.and(account.balance.loe(maxBalance));
        }

        List<Account> items = queryFactory
                .selectFrom(account)
                .where(where)
                .orderBy(resolveOrder(account, sortBy, sortDir))
                .offset((long) page * size)
                .limit(size)
                .fetch();

        Long total = queryFactory
                .select(account.count())
                .from(account)
                .where(where)
                .fetchOne();

        return new PageImpl<>(items, PageRequest.of(page, size), total == null ? 0 : total);
    }

    private OrderSpecifier<?> resolveOrder(QAccount account, String sortBy, String sortDir) {
        Order direction = "asc".equalsIgnoreCase(sortDir) ? Order.ASC : Order.DESC;
        String normalizedSortBy = sortBy == null ? "createdAt" : sortBy;

        return switch (normalizedSortBy) {
            case "accountNumber" -> new OrderSpecifier<>(direction, account.accountNumber);
            case "balance" -> new OrderSpecifier<>(direction, account.balance);
            case "currency" -> new OrderSpecifier<>(direction, account.currency);
            default -> new OrderSpecifier<>(direction, account.createdAt);
        };
    }
}
