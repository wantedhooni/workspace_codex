package com.derivops.mvp.account.infrastructure;
import com.derivops.mvp.account.*;
import com.derivops.mvp.account.api.*;
import com.derivops.mvp.account.application.*;
import com.derivops.mvp.account.dto.*;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import com.derivops.mvp.common.rsql.QuerydslRsqlPredicateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QAccount account = QAccount.account;
    private static final Map<String, Expression<?>> FILTER_FIELDS = Map.ofEntries(
            Map.entry("id", account.id),
            Map.entry("accountNo", account.accountNo),
            Map.entry("broker", account.broker),
            Map.entry("status", account.status),
            Map.entry("ownerName", account.ownerName),
            Map.entry("openedAt", account.openedAt),
            Map.entry("closedAt", account.closedAt)
    );

    @Override
    public Page<Account> search(AccountStatus status, String broker, String keyword, String filter, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        if (status != null) {
            builder.and(account.status.eq(status));
        }
        if (broker != null && !broker.isBlank()) {
            builder.and(account.broker.equalsIgnoreCase(broker.trim()));
        }
        if (keyword != null && !keyword.isBlank()) {
            String normalized = keyword.trim();
            builder.and(
                    account.accountNo.containsIgnoreCase(normalized)
                            .or(account.ownerName.containsIgnoreCase(normalized))
                            .or(account.broker.containsIgnoreCase(normalized))
            );
        }
        if (filter != null && !filter.isBlank()) {
            builder.and(QuerydslRsqlPredicateBuilder.build(filter, FILTER_FIELDS));
        }

        List<Account> items = queryFactory
                .selectFrom(account)
                .where(builder)
                .orderBy(account.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(account.count())
                .from(account)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(items, pageable, total == null ? 0L : total);
    }
}
