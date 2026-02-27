package com.portal.admin.repo;

import com.portal.admin.domain.QRole;
import com.portal.admin.domain.Role;
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

public class RoleRepositoryImpl implements SearchableRepository<Role>, FieldSearchableRepository<Role> {

    private final JPAQueryFactory queryFactory;

    public RoleRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<Role> search(String query) {
        QRole role = QRole.role;
        return queryFactory
                .selectFrom(role)
                .where(
                        role.name.containsIgnoreCase(query)
                                .or(role.description.containsIgnoreCase(query))
                )
                .orderBy(role.id.asc())
                .fetch();
    }

    @Override
    public Page<Role> search(String query, Pageable pageable) {
        QRole role = QRole.role;
        BooleanBuilder where = new BooleanBuilder()
                .and(
                        role.name.containsIgnoreCase(query)
                                .or(role.description.containsIgnoreCase(query))
                );
        return fetchPage(role, where, pageable);
    }

    @Override
    public List<Role> searchByFields(Map<String, String> filters) {
        QRole role = QRole.role;
        BooleanBuilder where = buildFieldWhere(role, filters);

        return queryFactory
                .selectFrom(role)
                .where(where)
                .orderBy(role.id.asc())
                .fetch();
    }

    @Override
    public Page<Role> searchByFields(Map<String, String> filters, Pageable pageable) {
        QRole role = QRole.role;
        BooleanBuilder where = buildFieldWhere(role, filters);
        return fetchPage(role, where, pageable);
    }

    @Override
    public Set<String> allowedSortFields() {
        return Set.of("id", "name", "description");
    }

    @Override
    public Set<String> allowedFilterFields() {
        return Set.of("name", "description");
    }

    private BooleanBuilder buildFieldWhere(QRole role, Map<String, String> filters) {
        BooleanBuilder where = new BooleanBuilder();

        String name = filters.get("name");
        if (name != null && !name.isBlank()) {
            where.and(role.name.containsIgnoreCase(name.trim()));
        }

        String description = filters.get("description");
        if (description != null && !description.isBlank()) {
            where.and(role.description.containsIgnoreCase(description.trim()));
        }

        return where;
    }

    private Page<Role> fetchPage(QRole role, BooleanBuilder where, Pageable pageable) {
        JPAQuery<Role> query = queryFactory
                .selectFrom(role)
                .where(where);

        applySort(query, pageable == null ? Sort.unsorted() : pageable.getSort(), role);
        if (pageable != null && pageable.isPaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }
        List<Role> content = query.fetch();

        Long total = queryFactory
                .select(role.id.count())
                .from(role)
                .where(where)
                .fetchOne();
        long totalCount = total == null ? 0L : total;
        Pageable resultPageable = pageable == null ? Pageable.unpaged() : pageable;
        return new PageImpl<>(content, resultPageable, totalCount);
    }

    private void applySort(JPAQuery<Role> query, Sort sort, QRole role) {
        if (sort == null || sort.isUnsorted()) {
            query.orderBy(role.id.asc());
            return;
        }

        for (Sort.Order order : sort) {
            boolean asc = order.isAscending();
            switch (order.getProperty()) {
                case "id" -> applyNumberSort(query, role.id, asc);
                case "name" -> query.orderBy(asc ? role.name.asc() : role.name.desc());
                case "description" -> query.orderBy(asc ? role.description.asc() : role.description.desc());
                default -> applyNumberSort(query, role.id, asc);
            }
        }
    }

    private <N extends Number & Comparable<?>> void applyNumberSort(
            JPAQuery<Role> query,
            NumberPath<N> path,
            boolean ascending
    ) {
        query.orderBy(ascending ? path.asc() : path.desc());
    }
}
