package com.portal.admin.repo;

import com.portal.admin.domain.ProgramItem;
import com.portal.admin.domain.QProgramItem;
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

public class ProgramItemRepositoryImpl implements SearchableRepository<ProgramItem>, FieldSearchableRepository<ProgramItem> {

    private final JPAQueryFactory queryFactory;

    public ProgramItemRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<ProgramItem> search(String query) {
        QProgramItem program = QProgramItem.programItem;
        return queryFactory
                .selectFrom(program)
                .where(
                        program.name.containsIgnoreCase(query)
                                .or(program.url.containsIgnoreCase(query))
                                .or(program.httpMethod.containsIgnoreCase(query))
                                .or(program.description.containsIgnoreCase(query))
                )
                .orderBy(program.id.asc())
                .fetch();
    }

    @Override
    public Page<ProgramItem> search(String query, Pageable pageable) {
        QProgramItem program = QProgramItem.programItem;
        BooleanBuilder where = new BooleanBuilder()
                .and(
                        program.name.containsIgnoreCase(query)
                                .or(program.url.containsIgnoreCase(query))
                                .or(program.httpMethod.containsIgnoreCase(query))
                                .or(program.description.containsIgnoreCase(query))
                );
        return fetchPage(program, where, pageable);
    }

    @Override
    public List<ProgramItem> searchByFields(Map<String, String> filters) {
        QProgramItem program = QProgramItem.programItem;
        BooleanBuilder where = buildFieldWhere(program, filters);

        return queryFactory
                .selectFrom(program)
                .where(where)
                .orderBy(program.id.asc())
                .fetch();
    }

    @Override
    public Page<ProgramItem> searchByFields(Map<String, String> filters, Pageable pageable) {
        QProgramItem program = QProgramItem.programItem;
        BooleanBuilder where = buildFieldWhere(program, filters);
        return fetchPage(program, where, pageable);
    }

    @Override
    public Set<String> allowedSortFields() {
        return Set.of("id", "name", "url", "httpMethod", "description", "enabled");
    }

    @Override
    public Set<String> allowedFilterFields() {
        return Set.of("name", "url", "httpMethod", "description", "enabled");
    }

    private BooleanBuilder buildFieldWhere(QProgramItem program, Map<String, String> filters) {
        BooleanBuilder where = new BooleanBuilder();

        String name = filters.get("name");
        if (name != null && !name.isBlank()) {
            where.and(program.name.containsIgnoreCase(name.trim()));
        }

        String url = filters.get("url");
        if (url != null && !url.isBlank()) {
            where.and(program.url.containsIgnoreCase(url.trim()));
        }

        String httpMethod = filters.get("httpMethod");
        if (httpMethod != null && !httpMethod.isBlank()) {
            where.and(program.httpMethod.eq(httpMethod.trim().toUpperCase()));
        }

        String description = filters.get("description");
        if (description != null && !description.isBlank()) {
            where.and(program.description.containsIgnoreCase(description.trim()));
        }

        String enabled = filters.get("enabled");
        if (enabled != null && ("true".equalsIgnoreCase(enabled) || "false".equalsIgnoreCase(enabled))) {
            where.and(program.enabled.eq(Boolean.parseBoolean(enabled)));
        }

        return where;
    }

    private Page<ProgramItem> fetchPage(QProgramItem program, BooleanBuilder where, Pageable pageable) {
        JPAQuery<ProgramItem> query = queryFactory
                .selectFrom(program)
                .where(where);

        applySort(query, pageable == null ? Sort.unsorted() : pageable.getSort(), program);
        if (pageable != null && pageable.isPaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }
        List<ProgramItem> content = query.fetch();

        Long total = queryFactory
                .select(program.id.count())
                .from(program)
                .where(where)
                .fetchOne();
        long totalCount = total == null ? 0L : total;
        Pageable resultPageable = pageable == null ? Pageable.unpaged() : pageable;
        return new PageImpl<>(content, resultPageable, totalCount);
    }

    private void applySort(JPAQuery<ProgramItem> query, Sort sort, QProgramItem program) {
        if (sort == null || sort.isUnsorted()) {
            query.orderBy(program.id.asc());
            return;
        }

        for (Sort.Order order : sort) {
            boolean asc = order.isAscending();
            switch (order.getProperty()) {
                case "id" -> applyNumberSort(query, program.id, asc);
                case "name" -> query.orderBy(asc ? program.name.asc() : program.name.desc());
                case "url" -> query.orderBy(asc ? program.url.asc() : program.url.desc());
                case "httpMethod" -> query.orderBy(asc ? program.httpMethod.asc() : program.httpMethod.desc());
                case "description" -> query.orderBy(asc ? program.description.asc() : program.description.desc());
                case "enabled" -> query.orderBy(asc ? program.enabled.asc() : program.enabled.desc());
                default -> applyNumberSort(query, program.id, asc);
            }
        }
    }

    private <N extends Number & Comparable<?>> void applyNumberSort(
            JPAQuery<ProgramItem> query,
            NumberPath<N> path,
            boolean ascending
    ) {
        query.orderBy(ascending ? path.asc() : path.desc());
    }
}
