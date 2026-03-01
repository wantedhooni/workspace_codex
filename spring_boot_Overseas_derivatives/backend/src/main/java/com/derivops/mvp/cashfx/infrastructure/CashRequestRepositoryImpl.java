package com.derivops.mvp.cashfx.infrastructure;
import com.derivops.mvp.cashfx.*;
import com.derivops.mvp.cashfx.api.*;
import com.derivops.mvp.cashfx.application.*;
import com.derivops.mvp.cashfx.dto.*;


import com.derivops.mvp.account.QAccount;
import com.derivops.mvp.common.rsql.QuerydslRsqlPredicateBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class CashRequestRepositoryImpl implements CashRequestRepositoryCustom {

    private static final QCashRequest cashRequest = QCashRequest.cashRequest;
    private static final QAccount account = QAccount.account;

    private static final Map<String, Expression<?>> FILTER_FIELDS = Map.ofEntries(
            Map.entry("id", cashRequest.id),
            Map.entry("status", cashRequest.status),
            Map.entry("accountId", account.id),
            Map.entry("accountNo", account.accountNo),
            Map.entry("accountBroker", account.broker),
            Map.entry("accountOwnerName", account.ownerName),
            Map.entry("accountStatus", account.status),
            Map.entry("type", cashRequest.type),
            Map.entry("currency", cashRequest.currency),
            Map.entry("amount", cashRequest.amount),
            Map.entry("priority", cashRequest.priority),
            Map.entry("valueDate", cashRequest.valueDate),
            Map.entry("manualReviewRequired", cashRequest.manualReviewRequired),
            Map.entry("controlReason", cashRequest.controlReason),
            Map.entry("controlPolicyId", cashRequest.controlPolicyId),
            Map.entry("controlPolicySource", cashRequest.controlPolicySource),
            Map.entry("controlLimitPolicyId", cashRequest.controlLimitPolicyId),
            Map.entry("controlLimitPolicySource", cashRequest.controlLimitPolicySource),
            Map.entry("projectedDailyExposure", cashRequest.projectedDailyExposure),
            Map.entry("slaDueAt", cashRequest.slaDueAt),
            Map.entry("requestedBy", cashRequest.requestedBy),
            Map.entry("reviewedBy", cashRequest.reviewedBy),
            Map.entry("requestedAt", cashRequest.requestedAt),
            Map.entry("reviewedAt", cashRequest.reviewedAt),
            Map.entry("reason", cashRequest.reason),
            Map.entry("reviewReason", cashRequest.reviewReason)
    );

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CashRequest> search(RequestStatus status, Long accountId, String keyword, String filter, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        if (status != null) {
            builder.and(cashRequest.status.eq(status));
        }
        if (accountId != null) {
            builder.and(account.id.eq(accountId));
        }
        if (keyword != null && !keyword.isBlank()) {
            String q = keyword.trim();
            builder.and(
                    cashRequest.requestedBy.containsIgnoreCase(q)
                            .or(cashRequest.reason.containsIgnoreCase(q))
                            .or(cashRequest.reviewReason.containsIgnoreCase(q))
                            .or(cashRequest.controlReason.containsIgnoreCase(q))
                            .or(cashRequest.currency.containsIgnoreCase(q))
                            .or(cashRequest.priority.stringValue().containsIgnoreCase(q))
                            .or(account.accountNo.containsIgnoreCase(q))
                            .or(account.broker.containsIgnoreCase(q))
                            .or(account.ownerName.containsIgnoreCase(q))
            );
        }
        if (filter != null && !filter.isBlank()) {
            builder.and(QuerydslRsqlPredicateBuilder.build(filter, FILTER_FIELDS));
        }

        List<CashRequest> items = queryFactory
                .selectFrom(cashRequest)
                .join(cashRequest.account, account).fetchJoin()
                .where(builder)
                .orderBy(cashRequest.requestedAt.desc(), cashRequest.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(cashRequest.count())
                .from(cashRequest)
                .join(cashRequest.account, account)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(items, pageable, total == null ? 0L : total);
    }

    @Override
    public BigDecimal sumExposure(Long accountId, CashRequestType type, String currency, LocalDate valueDate) {
        BigDecimal result = queryFactory
                .select(cashRequest.amount.sum())
                .from(cashRequest)
                .where(
                        cashRequest.account.id.eq(accountId),
                        cashRequest.type.eq(type),
                        cashRequest.currency.eq(currency),
                        cashRequest.valueDate.eq(valueDate),
                        cashRequest.status.in(RequestStatus.PENDING, RequestStatus.APPROVED)
                )
                .fetchOne();

        return result == null ? BigDecimal.ZERO : result;
    }
}
