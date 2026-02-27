package com.portal.admin.repo;

import com.portal.admin.domain.LoginPolicy;
import com.portal.admin.domain.QLoginPolicy;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoginPolicyRepositoryImpl implements SearchableRepository<LoginPolicy>, FieldSearchableRepository<LoginPolicy> {

    private final JPAQueryFactory queryFactory;

    public LoginPolicyRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<LoginPolicy> search(String query) {
        QLoginPolicy loginPolicy = QLoginPolicy.loginPolicy;
        return queryFactory
                .selectFrom(loginPolicy)
                .where(
                        loginPolicy.name.containsIgnoreCase(query)
                                .or(loginPolicy.allowedIpCidr.containsIgnoreCase(query))
                )
                .orderBy(loginPolicy.id.asc())
                .fetch();
    }

    @Override
    public Page<LoginPolicy> search(String query, Pageable pageable) {
        QLoginPolicy loginPolicy = QLoginPolicy.loginPolicy;
        BooleanBuilder where = new BooleanBuilder()
                .and(
                        loginPolicy.name.containsIgnoreCase(query)
                                .or(loginPolicy.allowedIpCidr.containsIgnoreCase(query))
                );
        return fetchPage(loginPolicy, where, pageable);
    }

    @Override
    public List<LoginPolicy> searchByFields(Map<String, String> filters) {
        QLoginPolicy loginPolicy = QLoginPolicy.loginPolicy;
        BooleanBuilder where = buildFieldWhere(loginPolicy, filters);

        return queryFactory
                .selectFrom(loginPolicy)
                .where(where)
                .orderBy(loginPolicy.id.asc())
                .fetch();
    }

    @Override
    public Page<LoginPolicy> searchByFields(Map<String, String> filters, Pageable pageable) {
        QLoginPolicy loginPolicy = QLoginPolicy.loginPolicy;
        BooleanBuilder where = buildFieldWhere(loginPolicy, filters);
        return fetchPage(loginPolicy, where, pageable);
    }

    @Override
    public Set<String> allowedSortFields() {
        return Set.of("id", "name", "maxFailCount", "lockMinutes", "allowedIpCidr", "enabled");
    }

    @Override
    public Set<String> allowedFilterFields() {
        return Set.of("name", "maxFailCount", "lockMinutes", "allowedIpCidr", "enabled");
    }

    private BooleanBuilder buildFieldWhere(QLoginPolicy loginPolicy, Map<String, String> filters) {
        BooleanBuilder where = new BooleanBuilder();

        String name = filters.get("name");
        if (name != null && !name.isBlank()) {
            where.and(loginPolicy.name.containsIgnoreCase(name.trim()));
        }

        String allowedIpCidr = filters.get("allowedIpCidr");
        if (allowedIpCidr != null && !allowedIpCidr.isBlank()) {
            where.and(loginPolicy.allowedIpCidr.containsIgnoreCase(allowedIpCidr.trim()));
        }

        String maxFailCount = filters.get("maxFailCount");
        if (maxFailCount != null) {
            try {
                where.and(loginPolicy.maxFailCount.eq(Integer.parseInt(maxFailCount.trim())));
            } catch (NumberFormatException ignored) {
            }
        }

        String lockMinutes = filters.get("lockMinutes");
        if (lockMinutes != null) {
            try {
                where.and(loginPolicy.lockMinutes.eq(Integer.parseInt(lockMinutes.trim())));
            } catch (NumberFormatException ignored) {
            }
        }

        String enabled = filters.get("enabled");
        if (enabled != null && ("true".equalsIgnoreCase(enabled) || "false".equalsIgnoreCase(enabled))) {
            where.and(loginPolicy.enabled.eq(Boolean.parseBoolean(enabled)));
        }

        return where;
    }

    private Page<LoginPolicy> fetchPage(QLoginPolicy loginPolicy, BooleanBuilder where, Pageable pageable) {
        JPAQuery<LoginPolicy> query = queryFactory
                .selectFrom(loginPolicy)
                .where(where);

        applySort(query, pageable == null ? Sort.unsorted() : pageable.getSort(), loginPolicy);
        if (pageable != null && pageable.isPaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }
        List<LoginPolicy> content = query.fetch();

        Long total = queryFactory
                .select(loginPolicy.id.count())
                .from(loginPolicy)
                .where(where)
                .fetchOne();
        long totalCount = total == null ? 0L : total;
        Pageable resultPageable = pageable == null ? Pageable.unpaged() : pageable;
        return new PageImpl<>(content, resultPageable, totalCount);
    }

    private void applySort(JPAQuery<LoginPolicy> query, Sort sort, QLoginPolicy loginPolicy) {
        if (sort == null || sort.isUnsorted()) {
            query.orderBy(loginPolicy.id.asc());
            return;
        }

        for (Sort.Order order : sort) {
            boolean asc = order.isAscending();
            switch (order.getProperty()) {
                case "id" -> applyNumberSort(query, loginPolicy.id, asc);
                case "name" -> query.orderBy(asc ? loginPolicy.name.asc() : loginPolicy.name.desc());
                case "maxFailCount" -> applyNumberSort(query, loginPolicy.maxFailCount, asc);
                case "lockMinutes" -> applyNumberSort(query, loginPolicy.lockMinutes, asc);
                case "allowedIpCidr" -> query.orderBy(asc ? loginPolicy.allowedIpCidr.asc() : loginPolicy.allowedIpCidr.desc());
                case "enabled" -> query.orderBy(asc ? loginPolicy.enabled.asc() : loginPolicy.enabled.desc());
                default -> applyNumberSort(query, loginPolicy.id, asc);
            }
        }
    }

    private <N extends Number & Comparable<?>> void applyNumberSort(
            JPAQuery<LoginPolicy> query,
            NumberPath<N> path,
            boolean ascending
    ) {
        query.orderBy(ascending ? path.asc() : path.desc());
    }
}
