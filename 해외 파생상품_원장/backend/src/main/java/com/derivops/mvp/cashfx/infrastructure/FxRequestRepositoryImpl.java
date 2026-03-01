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
public class FxRequestRepositoryImpl implements FxRequestRepositoryCustom {

    private static final QFxRequest fxRequest = QFxRequest.fxRequest;
    private static final QAccount account = QAccount.account;

    private static final Map<String, Expression<?>> FILTER_FIELDS = Map.ofEntries(
            Map.entry("id", fxRequest.id),
            Map.entry("status", fxRequest.status),
            Map.entry("accountId", account.id),
            Map.entry("accountNo", account.accountNo),
            Map.entry("accountBroker", account.broker),
            Map.entry("accountOwnerName", account.ownerName),
            Map.entry("accountStatus", account.status),
            Map.entry("fromCurrency", fxRequest.fromCurrency),
            Map.entry("toCurrency", fxRequest.toCurrency),
            Map.entry("amount", fxRequest.amount),
            Map.entry("priority", fxRequest.priority),
            Map.entry("valueDate", fxRequest.valueDate),
            Map.entry("manualReviewRequired", fxRequest.manualReviewRequired),
            Map.entry("controlReason", fxRequest.controlReason),
            Map.entry("controlPolicyId", fxRequest.controlPolicyId),
            Map.entry("controlPolicySource", fxRequest.controlPolicySource),
            Map.entry("controlLimitPolicyId", fxRequest.controlLimitPolicyId),
            Map.entry("controlLimitPolicySource", fxRequest.controlLimitPolicySource),
            Map.entry("projectedDailyExposure", fxRequest.projectedDailyExposure),
            Map.entry("slaDueAt", fxRequest.slaDueAt),
            Map.entry("requestedBy", fxRequest.requestedBy),
            Map.entry("reviewedBy", fxRequest.reviewedBy),
            Map.entry("requestedAt", fxRequest.requestedAt),
            Map.entry("reviewedAt", fxRequest.reviewedAt),
            Map.entry("reason", fxRequest.reason),
            Map.entry("reviewReason", fxRequest.reviewReason)
    );

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<FxRequest> search(RequestStatus status, Long accountId, String keyword, String filter, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        if (status != null) {
            builder.and(fxRequest.status.eq(status));
        }
        if (accountId != null) {
            builder.and(account.id.eq(accountId));
        }
        if (keyword != null && !keyword.isBlank()) {
            String q = keyword.trim();
            builder.and(
                    fxRequest.requestedBy.containsIgnoreCase(q)
                            .or(fxRequest.reason.containsIgnoreCase(q))
                            .or(fxRequest.reviewReason.containsIgnoreCase(q))
                            .or(fxRequest.controlReason.containsIgnoreCase(q))
                            .or(fxRequest.fromCurrency.containsIgnoreCase(q))
                            .or(fxRequest.toCurrency.containsIgnoreCase(q))
                            .or(fxRequest.priority.stringValue().containsIgnoreCase(q))
                            .or(account.accountNo.containsIgnoreCase(q))
                            .or(account.broker.containsIgnoreCase(q))
                            .or(account.ownerName.containsIgnoreCase(q))
            );
        }
        if (filter != null && !filter.isBlank()) {
            builder.and(QuerydslRsqlPredicateBuilder.build(filter, FILTER_FIELDS));
        }

        List<FxRequest> items = queryFactory
                .selectFrom(fxRequest)
                .join(fxRequest.account, account).fetchJoin()
                .where(builder)
                .orderBy(fxRequest.requestedAt.desc(), fxRequest.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(fxRequest.count())
                .from(fxRequest)
                .join(fxRequest.account, account)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(items, pageable, total == null ? 0L : total);
    }

    @Override
    public BigDecimal sumExposure(Long accountId, String fromCurrency, LocalDate valueDate) {
        BigDecimal result = queryFactory
                .select(fxRequest.amount.sum())
                .from(fxRequest)
                .where(
                        fxRequest.account.id.eq(accountId),
                        fxRequest.fromCurrency.eq(fromCurrency),
                        fxRequest.valueDate.eq(valueDate),
                        fxRequest.status.in(RequestStatus.PENDING, RequestStatus.APPROVED)
                )
                .fetchOne();

        return result == null ? BigDecimal.ZERO : result;
    }
}
