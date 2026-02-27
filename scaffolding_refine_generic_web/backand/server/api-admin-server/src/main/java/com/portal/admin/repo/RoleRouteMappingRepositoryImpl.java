package com.portal.admin.repo;

import com.portal.admin.domain.QRoleRouteMapping;
import com.portal.admin.domain.RoleRouteMapping;
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

public class RoleRouteMappingRepositoryImpl implements SearchableRepository<RoleRouteMapping>, FieldSearchableRepository<RoleRouteMapping> {

    private final JPAQueryFactory queryFactory;

    public RoleRouteMappingRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<RoleRouteMapping> search(String query) {
        QRoleRouteMapping roleRoute = QRoleRouteMapping.roleRouteMapping;
        return queryFactory
                .selectFrom(roleRoute)
                .where(
                        roleRoute.pattern.containsIgnoreCase(query)
                                .or(roleRoute.httpMethod.containsIgnoreCase(query))
                                .or(roleRoute.description.containsIgnoreCase(query))
                )
                .orderBy(roleRoute.id.asc())
                .fetch();
    }

    @Override
    public Page<RoleRouteMapping> search(String query, Pageable pageable) {
        QRoleRouteMapping roleRoute = QRoleRouteMapping.roleRouteMapping;
        BooleanBuilder where = new BooleanBuilder()
                .and(
                        roleRoute.pattern.containsIgnoreCase(query)
                                .or(roleRoute.httpMethod.containsIgnoreCase(query))
                                .or(roleRoute.description.containsIgnoreCase(query))
                );
        return fetchPage(roleRoute, where, pageable);
    }

    @Override
    public List<RoleRouteMapping> searchByFields(Map<String, String> filters) {
        QRoleRouteMapping roleRoute = QRoleRouteMapping.roleRouteMapping;
        BooleanBuilder where = buildFieldWhere(roleRoute, filters);

        return queryFactory
                .selectFrom(roleRoute)
                .where(where)
                .orderBy(roleRoute.id.asc())
                .fetch();
    }

    @Override
    public Page<RoleRouteMapping> searchByFields(Map<String, String> filters, Pageable pageable) {
        QRoleRouteMapping roleRoute = QRoleRouteMapping.roleRouteMapping;
        BooleanBuilder where = buildFieldWhere(roleRoute, filters);
        return fetchPage(roleRoute, where, pageable);
    }

    @Override
    public Set<String> allowedSortFields() {
        return Set.of("id", "roleId", "pattern", "httpMethod", "description", "enabled");
    }

    @Override
    public Set<String> allowedFilterFields() {
        return Set.of("roleId", "pattern", "httpMethod", "description", "enabled");
    }

    private BooleanBuilder buildFieldWhere(QRoleRouteMapping roleRoute, Map<String, String> filters) {
        BooleanBuilder where = new BooleanBuilder();

        String roleId = filters.get("roleId");
        if (roleId != null) {
            try {
                where.and(roleRoute.roleId.eq(Long.parseLong(roleId.trim())));
            } catch (NumberFormatException ignored) {
            }
        }

        String pattern = filters.get("pattern");
        if (pattern != null && !pattern.isBlank()) {
            where.and(roleRoute.pattern.containsIgnoreCase(pattern.trim()));
        }

        String httpMethod = filters.get("httpMethod");
        if (httpMethod != null && !httpMethod.isBlank()) {
            where.and(roleRoute.httpMethod.eq(httpMethod.trim().toUpperCase()));
        }

        String description = filters.get("description");
        if (description != null && !description.isBlank()) {
            where.and(roleRoute.description.containsIgnoreCase(description.trim()));
        }

        String enabled = filters.get("enabled");
        if (enabled != null && ("true".equalsIgnoreCase(enabled) || "false".equalsIgnoreCase(enabled))) {
            where.and(roleRoute.enabled.eq(Boolean.parseBoolean(enabled)));
        }

        return where;
    }

    private Page<RoleRouteMapping> fetchPage(QRoleRouteMapping roleRoute, BooleanBuilder where, Pageable pageable) {
        JPAQuery<RoleRouteMapping> query = queryFactory
                .selectFrom(roleRoute)
                .where(where);

        applySort(query, pageable == null ? Sort.unsorted() : pageable.getSort(), roleRoute);
        if (pageable != null && pageable.isPaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }
        List<RoleRouteMapping> content = query.fetch();

        Long total = queryFactory
                .select(roleRoute.id.count())
                .from(roleRoute)
                .where(where)
                .fetchOne();
        long totalCount = total == null ? 0L : total;
        Pageable resultPageable = pageable == null ? Pageable.unpaged() : pageable;
        return new PageImpl<>(content, resultPageable, totalCount);
    }

    private void applySort(JPAQuery<RoleRouteMapping> query, Sort sort, QRoleRouteMapping roleRoute) {
        if (sort == null || sort.isUnsorted()) {
            query.orderBy(roleRoute.id.asc());
            return;
        }

        for (Sort.Order order : sort) {
            boolean asc = order.isAscending();
            switch (order.getProperty()) {
                case "id" -> applyNumberSort(query, roleRoute.id, asc);
                case "roleId" -> applyNumberSort(query, roleRoute.roleId, asc);
                case "pattern" -> query.orderBy(asc ? roleRoute.pattern.asc() : roleRoute.pattern.desc());
                case "httpMethod" -> query.orderBy(asc ? roleRoute.httpMethod.asc() : roleRoute.httpMethod.desc());
                case "description" -> query.orderBy(asc ? roleRoute.description.asc() : roleRoute.description.desc());
                case "enabled" -> query.orderBy(asc ? roleRoute.enabled.asc() : roleRoute.enabled.desc());
                default -> applyNumberSort(query, roleRoute.id, asc);
            }
        }
    }

    private <N extends Number & Comparable<?>> void applyNumberSort(
            JPAQuery<RoleRouteMapping> query,
            NumberPath<N> path,
            boolean ascending
    ) {
        query.orderBy(ascending ? path.asc() : path.desc());
    }
}
