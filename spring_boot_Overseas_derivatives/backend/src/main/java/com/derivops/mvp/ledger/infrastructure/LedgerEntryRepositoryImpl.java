package com.derivops.mvp.ledger.infrastructure;

import com.derivops.mvp.account.QAccount;
import com.derivops.mvp.common.rsql.QuerydslRsqlPredicateBuilder;
import com.derivops.mvp.ledger.LedgerEntry;
import com.derivops.mvp.ledger.QLedgerEntry;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class LedgerEntryRepositoryImpl implements LedgerEntryRepositoryCustom {

    private static final QLedgerEntry ledgerEntry = QLedgerEntry.ledgerEntry;
    private static final QAccount account = QAccount.account;
    private static final Map<String, Expression<?>> FILTER_FIELDS = Map.ofEntries(
            Map.entry("id", ledgerEntry.id),
            Map.entry("entryNo", ledgerEntry.entryNo),
            Map.entry("accountId", ledgerEntry.account.id),
            Map.entry("referenceType", ledgerEntry.referenceType),
            Map.entry("referenceId", ledgerEntry.referenceId),
            Map.entry("symbol", ledgerEntry.symbol),
            Map.entry("postingDate", ledgerEntry.postingDate),
            Map.entry("currency", ledgerEntry.currency),
            Map.entry("quantityChange", ledgerEntry.quantityChange),
            Map.entry("amountChange", ledgerEntry.amountChange),
            Map.entry("runningQuantity", ledgerEntry.runningQuantity),
            Map.entry("runningAmount", ledgerEntry.runningAmount),
            Map.entry("accountNo", ledgerEntry.account.accountNo)
    );

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<LedgerEntry> search(Long accountId, String referenceId, String filter, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        if (accountId != null) {
            builder.and(ledgerEntry.account.id.eq(accountId));
        }
        if (referenceId != null && !referenceId.isBlank()) {
            builder.and(ledgerEntry.referenceId.eq(referenceId.trim()));
        }
        if (filter != null && !filter.isBlank()) {
            builder.and(QuerydslRsqlPredicateBuilder.build(filter, FILTER_FIELDS));
        }

        List<LedgerEntry> items = queryFactory
                .selectFrom(ledgerEntry)
                .join(ledgerEntry.account, account).fetchJoin()
                .where(builder)
                .orderBy(ledgerEntry.postingDate.desc(), ledgerEntry.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(ledgerEntry.count())
                .from(ledgerEntry)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(items, pageable, total == null ? 0L : total);
    }
}
