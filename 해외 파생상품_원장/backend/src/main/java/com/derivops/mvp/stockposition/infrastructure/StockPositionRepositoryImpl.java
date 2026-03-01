package com.derivops.mvp.stockposition.infrastructure;

import com.derivops.mvp.account.QAccount;
import com.derivops.mvp.common.rsql.QuerydslRsqlPredicateBuilder;
import com.derivops.mvp.stockposition.QStockPosition;
import com.derivops.mvp.stockposition.StockPosition;
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
public class StockPositionRepositoryImpl implements StockPositionRepositoryCustom {

    private static final QStockPosition stockPosition = QStockPosition.stockPosition;
    private static final QAccount account = QAccount.account;
    private static final Map<String, Expression<?>> FILTER_FIELDS = Map.ofEntries(
            Map.entry("id", stockPosition.id),
            Map.entry("accountId", stockPosition.account.id),
            Map.entry("symbol", stockPosition.symbol),
            Map.entry("market", stockPosition.market),
            Map.entry("currency", stockPosition.currency),
            Map.entry("quantity", stockPosition.quantity),
            Map.entry("averagePrice", stockPosition.averagePrice),
            Map.entry("totalCost", stockPosition.totalCost),
            Map.entry("lastTradeDate", stockPosition.lastTradeDate),
            Map.entry("accountNo", stockPosition.account.accountNo)
    );

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<StockPosition> search(Long accountId, String symbol, String filter, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        if (accountId != null) {
            builder.and(stockPosition.account.id.eq(accountId));
        }
        if (symbol != null && !symbol.isBlank()) {
            builder.and(stockPosition.symbol.equalsIgnoreCase(symbol.trim()));
        }
        if (filter != null && !filter.isBlank()) {
            builder.and(QuerydslRsqlPredicateBuilder.build(filter, FILTER_FIELDS));
        }

        List<StockPosition> items = queryFactory
                .selectFrom(stockPosition)
                .join(stockPosition.account, account).fetchJoin()
                .where(builder)
                .orderBy(stockPosition.totalCost.desc(), stockPosition.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(stockPosition.count())
                .from(stockPosition)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(items, pageable, total == null ? 0L : total);
    }
}
