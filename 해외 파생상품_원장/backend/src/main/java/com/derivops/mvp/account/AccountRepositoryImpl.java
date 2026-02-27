package com.derivops.mvp.account;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Account> search(AccountStatus status, String broker, String keyword, Pageable pageable) {
        QAccount account = QAccount.account;

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
