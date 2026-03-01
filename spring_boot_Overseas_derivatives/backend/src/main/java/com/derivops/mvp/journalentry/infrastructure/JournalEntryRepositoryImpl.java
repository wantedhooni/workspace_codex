package com.derivops.mvp.journalentry.infrastructure;

import com.derivops.mvp.account.QAccount;
import com.derivops.mvp.common.rsql.QuerydslRsqlPredicateBuilder;
import com.derivops.mvp.journalentry.JournalEntry;
import com.derivops.mvp.journalentry.QJournalEntry;
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
public class JournalEntryRepositoryImpl implements JournalEntryRepositoryCustom {

    private static final QJournalEntry journalEntry = QJournalEntry.journalEntry;
    private static final QAccount account = QAccount.account;
    private static final Map<String, Expression<?>> FILTER_FIELDS = Map.ofEntries(
            Map.entry("id", journalEntry.id),
            Map.entry("journalNo", journalEntry.journalNo),
            Map.entry("lineNo", journalEntry.lineNo),
            Map.entry("accountId", journalEntry.account.id),
            Map.entry("referenceType", journalEntry.referenceType),
            Map.entry("referenceId", journalEntry.referenceId),
            Map.entry("postingDate", journalEntry.postingDate),
            Map.entry("accountCode", journalEntry.accountCode),
            Map.entry("debitAmount", journalEntry.debitAmount),
            Map.entry("creditAmount", journalEntry.creditAmount),
            Map.entry("currency", journalEntry.currency),
            Map.entry("accountNo", journalEntry.account.accountNo)
    );

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<JournalEntry> search(Long accountId, String journalNo, String referenceId, String filter, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        if (accountId != null) {
            builder.and(journalEntry.account.id.eq(accountId));
        }
        if (journalNo != null && !journalNo.isBlank()) {
            builder.and(journalEntry.journalNo.equalsIgnoreCase(journalNo.trim()));
        }
        if (referenceId != null && !referenceId.isBlank()) {
            builder.and(journalEntry.referenceId.eq(referenceId.trim()));
        }
        if (filter != null && !filter.isBlank()) {
            builder.and(QuerydslRsqlPredicateBuilder.build(filter, FILTER_FIELDS));
        }

        List<JournalEntry> items = queryFactory
                .selectFrom(journalEntry)
                .join(journalEntry.account, account).fetchJoin()
                .where(builder)
                .orderBy(journalEntry.postingDate.desc(), journalEntry.journalNo.desc(), journalEntry.lineNo.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(journalEntry.count())
                .from(journalEntry)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(items, pageable, total == null ? 0L : total);
    }
}
