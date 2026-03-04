package com.revy.mvpbanking.customer.domain;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class AdminCustomerQueryRepository {

    private final JPAQueryFactory queryFactory;

    public AdminCustomerQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Page<Customer> search(
            String query,
            CustomerStatus status,
            Instant createdFrom,
            Instant createdTo,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        QCustomer customer = QCustomer.customer;
        BooleanBuilder where = baseFilter(customer, query, status, createdFrom, createdTo);

        List<Customer> items = queryFactory
                .selectFrom(customer)
                .where(where)
                .orderBy(resolveOrder(customer, sortBy, sortDir))
                .offset((long) page * size)
                .limit(size)
                .fetch();

        Long total = queryFactory
                .select(customer.count())
                .from(customer)
                .where(where)
                .fetchOne();

        return new PageImpl<>(items, PageRequest.of(page, size), total == null ? 0 : total);
    }

    public CustomerStatusSummary summarize(String query, Instant createdFrom, Instant createdTo) {
        QCustomer customer = QCustomer.customer;
        BooleanBuilder where = baseFilter(customer, query, null, createdFrom, createdTo);
        NumberExpression<Long> countExpression = customer.count();

        List<Tuple> rows = queryFactory
                .select(customer.status, countExpression)
                .from(customer)
                .where(where)
                .groupBy(customer.status)
                .fetch();

        long active = 0;
        long reviewRequired = 0;
        long suspended = 0;

        for (Tuple row : rows) {
            CustomerStatus currentStatus = row.get(customer.status);
            Long count = row.get(countExpression);
            if (currentStatus == null || count == null) {
                continue;
            }

            switch (currentStatus) {
                case ACTIVE -> active = count;
                case REVIEW_REQUIRED -> reviewRequired = count;
                case SUSPENDED -> suspended = count;
            }
        }

        return new CustomerStatusSummary(active, reviewRequired, suspended, active + reviewRequired + suspended);
    }

    private BooleanBuilder baseFilter(
            QCustomer customer,
            String query,
            CustomerStatus status,
            Instant createdFrom,
            Instant createdTo
    ) {
        BooleanBuilder where = new BooleanBuilder();

        if (query != null && !query.isBlank()) {
            where.and(
                    customer.customerNumber.containsIgnoreCase(query)
                            .or(customer.fullName.containsIgnoreCase(query))
                            .or(customer.email.containsIgnoreCase(query))
            );
        }

        if (status != null) {
            where.and(customer.status.eq(status));
        }

        if (createdFrom != null) {
            where.and(customer.createdAt.goe(createdFrom));
        }

        if (createdTo != null) {
            where.and(customer.createdAt.lt(createdTo));
        }

        return where;
    }

    private OrderSpecifier<?> resolveOrder(QCustomer customer, String sortBy, String sortDir) {
        Order direction = "asc".equalsIgnoreCase(sortDir) ? Order.ASC : Order.DESC;
        String normalizedSortBy = sortBy == null ? "createdAt" : sortBy;

        return switch (normalizedSortBy) {
            case "customerNumber" -> new OrderSpecifier<>(direction, customer.customerNumber);
            case "fullName" -> new OrderSpecifier<>(direction, customer.fullName);
            case "email" -> new OrderSpecifier<>(direction, customer.email);
            default -> new OrderSpecifier<>(direction, customer.createdAt);
        };
    }

    public record CustomerStatusSummary(
            long active,
            long reviewRequired,
            long suspended,
            long total
    ) {
    }
}
