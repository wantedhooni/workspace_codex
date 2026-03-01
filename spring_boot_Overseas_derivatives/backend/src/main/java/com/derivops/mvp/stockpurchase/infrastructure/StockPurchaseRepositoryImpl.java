package com.derivops.mvp.stockpurchase.infrastructure;

import com.derivops.mvp.account.QAccount;
import com.derivops.mvp.common.rsql.QuerydslRsqlPredicateBuilder;
import com.derivops.mvp.stockpurchase.QStockPurchase;
import com.derivops.mvp.stockpurchase.StockPurchase;
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
public class StockPurchaseRepositoryImpl implements StockPurchaseRepositoryCustom {

    private static final QStockPurchase stockPurchase = QStockPurchase.stockPurchase;
    private static final QAccount account = QAccount.account;
    private static final Map<String, Expression<?>> FILTER_FIELDS = Map.ofEntries(
            Map.entry("id", stockPurchase.id),
            Map.entry("accountId", stockPurchase.account.id),
            Map.entry("symbol", stockPurchase.symbol),
            Map.entry("market", stockPurchase.market),
            Map.entry("currency", stockPurchase.currency),
            Map.entry("tradeDate", stockPurchase.tradeDate),
            Map.entry("settlementDate", stockPurchase.settlementDate),
            Map.entry("quantity", stockPurchase.quantity),
            Map.entry("price", stockPurchase.price),
            Map.entry("grossAmount", stockPurchase.grossAmount),
            Map.entry("feeAmount", stockPurchase.feeAmount),
            Map.entry("netAmount", stockPurchase.netAmount),
            Map.entry("brokerOrderNo", stockPurchase.brokerOrderNo),
            Map.entry("createdBy", stockPurchase.createdBy),
            Map.entry("accountNo", stockPurchase.account.accountNo)
    );

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<StockPurchase> search(Long accountId, String symbol, String keyword, String filter, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        if (accountId != null) {
            builder.and(stockPurchase.account.id.eq(accountId));
        }
        if (symbol != null && !symbol.isBlank()) {
            builder.and(stockPurchase.symbol.equalsIgnoreCase(symbol.trim()));
        }
        if (keyword != null && !keyword.isBlank()) {
            String q = keyword.trim();
            builder.and(
                    stockPurchase.symbol.containsIgnoreCase(q)
                            .or(stockPurchase.market.containsIgnoreCase(q))
                            .or(stockPurchase.currency.containsIgnoreCase(q))
                            .or(stockPurchase.brokerOrderNo.containsIgnoreCase(q))
                            .or(stockPurchase.account.accountNo.containsIgnoreCase(q))
            );
        }
        if (filter != null && !filter.isBlank()) {
            builder.and(QuerydslRsqlPredicateBuilder.build(filter, FILTER_FIELDS));
        }

        List<StockPurchase> items = queryFactory
                .selectFrom(stockPurchase)
                .join(stockPurchase.account, account).fetchJoin()
                .where(builder)
                .orderBy(stockPurchase.tradeDate.desc(), stockPurchase.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(stockPurchase.count())
                .from(stockPurchase)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(items, pageable, total == null ? 0L : total);
    }
}
