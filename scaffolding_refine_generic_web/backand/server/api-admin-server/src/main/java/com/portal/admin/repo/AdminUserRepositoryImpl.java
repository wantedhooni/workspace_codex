package com.portal.admin.repo;

import com.portal.admin.domain.AdminUser;
import com.portal.admin.domain.QAdminUser;
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

public class AdminUserRepositoryImpl implements SearchableRepository<AdminUser>, FieldSearchableRepository<AdminUser> {

    private final JPAQueryFactory queryFactory;

    public AdminUserRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<AdminUser> search(String query) {
        QAdminUser user = QAdminUser.adminUser;
        return queryFactory
                .selectFrom(user)
                .where(user.username.containsIgnoreCase(query))
                .orderBy(user.id.asc())
                .fetch();
    }

    @Override
    public Page<AdminUser> search(String query, Pageable pageable) {
        QAdminUser user = QAdminUser.adminUser;
        BooleanBuilder where = new BooleanBuilder()
                .and(user.username.containsIgnoreCase(query));
        return fetchPage(user, where, pageable);
    }

    @Override
    public List<AdminUser> searchByFields(Map<String, String> filters) {
        QAdminUser user = QAdminUser.adminUser;
        BooleanBuilder where = buildFieldWhere(user, filters);
        return queryFactory
                .selectFrom(user)
                .where(where)
                .orderBy(user.id.asc())
                .fetch();
    }

    @Override
    public Page<AdminUser> searchByFields(Map<String, String> filters, Pageable pageable) {
        QAdminUser user = QAdminUser.adminUser;
        BooleanBuilder where = buildFieldWhere(user, filters);
        return fetchPage(user, where, pageable);
    }

    @Override
    public Set<String> allowedSortFields() {
        return Set.of("id", "username", "tokenVersion");
    }

    @Override
    public Set<String> allowedFilterFields() {
        return Set.of("username");
    }

    private BooleanBuilder buildFieldWhere(QAdminUser user, Map<String, String> filters) {
        BooleanBuilder where = new BooleanBuilder();
        String username = filters.get("username");
        if (username != null && !username.isBlank()) {
            where.and(user.username.containsIgnoreCase(username.trim()));
        }
        return where;
    }

    private Page<AdminUser> fetchPage(QAdminUser user, BooleanBuilder where, Pageable pageable) {
        JPAQuery<AdminUser> query = queryFactory
                .selectFrom(user)
                .where(where);

        applySort(query, pageable == null ? Sort.unsorted() : pageable.getSort(), user);
        if (pageable != null && pageable.isPaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }
        List<AdminUser> content = query.fetch();

        Long total = queryFactory
                .select(user.id.count())
                .from(user)
                .where(where)
                .fetchOne();
        long totalCount = total == null ? 0L : total;
        Pageable resultPageable = pageable == null ? Pageable.unpaged() : pageable;
        return new PageImpl<>(content, resultPageable, totalCount);
    }

    private void applySort(JPAQuery<AdminUser> query, Sort sort, QAdminUser user) {
        if (sort == null || sort.isUnsorted()) {
            query.orderBy(user.id.asc());
            return;
        }

        for (Sort.Order order : sort) {
            boolean asc = order.isAscending();
            switch (order.getProperty()) {
                case "id" -> applyNumberSort(query, user.id, asc);
                case "username" -> query.orderBy(asc ? user.username.asc() : user.username.desc());
                case "tokenVersion" -> applyNumberSort(query, user.tokenVersion, asc);
                default -> applyNumberSort(query, user.id, asc);
            }
        }
    }

    private <N extends Number & Comparable<?>> void applyNumberSort(
            JPAQuery<AdminUser> query,
            NumberPath<N> path,
            boolean ascending
    ) {
        query.orderBy(ascending ? path.asc() : path.desc());
    }
}
