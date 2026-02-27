package com.portal.admin.repo;

import com.portal.admin.domain.Permission;
import com.portal.admin.domain.QPermission;
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

public class PermissionRepositoryImpl implements SearchableRepository<Permission>, FieldSearchableRepository<Permission> {

    private final JPAQueryFactory queryFactory;

    public PermissionRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<Permission> search(String query) {
        QPermission permission = QPermission.permission;
        return queryFactory
                .selectFrom(permission)
                .where(
                        permission.code.containsIgnoreCase(query)
                                .or(permission.description.containsIgnoreCase(query))
                )
                .orderBy(permission.id.asc())
                .fetch();
    }

    @Override
    public Page<Permission> search(String query, Pageable pageable) {
        QPermission permission = QPermission.permission;
        BooleanBuilder where = new BooleanBuilder()
                .and(
                        permission.code.containsIgnoreCase(query)
                                .or(permission.description.containsIgnoreCase(query))
                );
        return fetchPage(permission, where, pageable);
    }

    @Override
    public List<Permission> searchByFields(Map<String, String> filters) {
        QPermission permission = QPermission.permission;
        BooleanBuilder where = buildFieldWhere(permission, filters);

        return queryFactory
                .selectFrom(permission)
                .where(where)
                .orderBy(permission.id.asc())
                .fetch();
    }

    @Override
    public Page<Permission> searchByFields(Map<String, String> filters, Pageable pageable) {
        QPermission permission = QPermission.permission;
        BooleanBuilder where = buildFieldWhere(permission, filters);
        return fetchPage(permission, where, pageable);
    }

    @Override
    public Set<String> allowedSortFields() {
        return Set.of("id", "code", "description");
    }

    @Override
    public Set<String> allowedFilterFields() {
        return Set.of("code", "description");
    }

    private BooleanBuilder buildFieldWhere(QPermission permission, Map<String, String> filters) {
        BooleanBuilder where = new BooleanBuilder();

        String code = filters.get("code");
        if (code != null && !code.isBlank()) {
            where.and(permission.code.containsIgnoreCase(code.trim()));
        }

        String description = filters.get("description");
        if (description != null && !description.isBlank()) {
            where.and(permission.description.containsIgnoreCase(description.trim()));
        }

        return where;
    }

    private Page<Permission> fetchPage(QPermission permission, BooleanBuilder where, Pageable pageable) {
        JPAQuery<Permission> query = queryFactory
                .selectFrom(permission)
                .where(where);

        applySort(query, pageable == null ? Sort.unsorted() : pageable.getSort(), permission);
        if (pageable != null && pageable.isPaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }
        List<Permission> content = query.fetch();

        Long total = queryFactory
                .select(permission.id.count())
                .from(permission)
                .where(where)
                .fetchOne();
        long totalCount = total == null ? 0L : total;
        Pageable resultPageable = pageable == null ? Pageable.unpaged() : pageable;
        return new PageImpl<>(content, resultPageable, totalCount);
    }

    private void applySort(JPAQuery<Permission> query, Sort sort, QPermission permission) {
        if (sort == null || sort.isUnsorted()) {
            query.orderBy(permission.id.asc());
            return;
        }

        for (Sort.Order order : sort) {
            boolean asc = order.isAscending();
            switch (order.getProperty()) {
                case "id" -> applyNumberSort(query, permission.id, asc);
                case "code" -> query.orderBy(asc ? permission.code.asc() : permission.code.desc());
                case "description" -> query.orderBy(asc ? permission.description.asc() : permission.description.desc());
                default -> applyNumberSort(query, permission.id, asc);
            }
        }
    }

    private <N extends Number & Comparable<?>> void applyNumberSort(
            JPAQuery<Permission> query,
            NumberPath<N> path,
            boolean ascending
    ) {
        query.orderBy(ascending ? path.asc() : path.desc());
    }
}
