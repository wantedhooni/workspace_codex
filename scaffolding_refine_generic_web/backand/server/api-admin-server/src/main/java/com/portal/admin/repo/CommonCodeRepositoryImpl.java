package com.portal.admin.repo;

import com.portal.admin.domain.CommonCode;
import com.portal.admin.domain.QCommonCode;
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

public class CommonCodeRepositoryImpl implements SearchableRepository<CommonCode>, FieldSearchableRepository<CommonCode> {

    private final JPAQueryFactory queryFactory;

    public CommonCodeRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<CommonCode> search(String query) {
        QCommonCode commonCode = QCommonCode.commonCode;
        return queryFactory
                .selectFrom(commonCode)
                .where(
                        commonCode.groupCode.containsIgnoreCase(query)
                                .or(commonCode.code.containsIgnoreCase(query))
                                .or(commonCode.name.containsIgnoreCase(query))
                                .or(commonCode.description.containsIgnoreCase(query))
                )
                .orderBy(commonCode.groupCode.asc(), commonCode.sortOrder.asc(), commonCode.id.asc())
                .fetch();
    }

    @Override
    public Page<CommonCode> search(String query, Pageable pageable) {
        QCommonCode commonCode = QCommonCode.commonCode;
        BooleanBuilder where = new BooleanBuilder()
                .and(
                        commonCode.groupCode.containsIgnoreCase(query)
                                .or(commonCode.code.containsIgnoreCase(query))
                                .or(commonCode.name.containsIgnoreCase(query))
                                .or(commonCode.description.containsIgnoreCase(query))
                );
        return fetchPage(commonCode, where, pageable);
    }

    @Override
    public List<CommonCode> searchByFields(Map<String, String> filters) {
        QCommonCode commonCode = QCommonCode.commonCode;
        BooleanBuilder where = buildFieldWhere(commonCode, filters);

        return queryFactory
                .selectFrom(commonCode)
                .where(where)
                .orderBy(commonCode.groupCode.asc(), commonCode.sortOrder.asc(), commonCode.id.asc())
                .fetch();
    }

    @Override
    public Page<CommonCode> searchByFields(Map<String, String> filters, Pageable pageable) {
        QCommonCode commonCode = QCommonCode.commonCode;
        BooleanBuilder where = buildFieldWhere(commonCode, filters);
        return fetchPage(commonCode, where, pageable);
    }

    @Override
    public Set<String> allowedSortFields() {
        return Set.of("id", "groupCode", "code", "name", "description", "sortOrder", "enabled");
    }

    @Override
    public Set<String> allowedFilterFields() {
        return Set.of("groupCode", "code", "name", "description", "enabled");
    }

    private BooleanBuilder buildFieldWhere(QCommonCode commonCode, Map<String, String> filters) {
        BooleanBuilder where = new BooleanBuilder();

        String groupCode = filters.get("groupCode");
        if (groupCode != null && !groupCode.isBlank()) {
            where.and(commonCode.groupCode.containsIgnoreCase(groupCode.trim()));
        }

        String code = filters.get("code");
        if (code != null && !code.isBlank()) {
            where.and(commonCode.code.containsIgnoreCase(code.trim()));
        }

        String name = filters.get("name");
        if (name != null && !name.isBlank()) {
            where.and(commonCode.name.containsIgnoreCase(name.trim()));
        }

        String description = filters.get("description");
        if (description != null && !description.isBlank()) {
            where.and(commonCode.description.containsIgnoreCase(description.trim()));
        }

        String enabled = filters.get("enabled");
        if (enabled != null && ("true".equalsIgnoreCase(enabled) || "false".equalsIgnoreCase(enabled))) {
            where.and(commonCode.enabled.eq(Boolean.parseBoolean(enabled)));
        }

        return where;
    }

    private Page<CommonCode> fetchPage(QCommonCode commonCode, BooleanBuilder where, Pageable pageable) {
        JPAQuery<CommonCode> query = queryFactory
                .selectFrom(commonCode)
                .where(where);

        applySort(query, pageable == null ? Sort.unsorted() : pageable.getSort(), commonCode);
        if (pageable != null && pageable.isPaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }
        List<CommonCode> content = query.fetch();

        Long total = queryFactory
                .select(commonCode.id.count())
                .from(commonCode)
                .where(where)
                .fetchOne();
        long totalCount = total == null ? 0L : total;
        Pageable resultPageable = pageable == null ? Pageable.unpaged() : pageable;
        return new PageImpl<>(content, resultPageable, totalCount);
    }

    private void applySort(JPAQuery<CommonCode> query, Sort sort, QCommonCode commonCode) {
        if (sort == null || sort.isUnsorted()) {
            query.orderBy(commonCode.groupCode.asc(), commonCode.sortOrder.asc(), commonCode.id.asc());
            return;
        }

        for (Sort.Order order : sort) {
            boolean asc = order.isAscending();
            switch (order.getProperty()) {
                case "id" -> applyNumberSort(query, commonCode.id, asc);
                case "groupCode" -> query.orderBy(asc ? commonCode.groupCode.asc() : commonCode.groupCode.desc());
                case "code" -> query.orderBy(asc ? commonCode.code.asc() : commonCode.code.desc());
                case "name" -> query.orderBy(asc ? commonCode.name.asc() : commonCode.name.desc());
                case "description" -> query.orderBy(asc ? commonCode.description.asc() : commonCode.description.desc());
                case "sortOrder" -> applyNumberSort(query, commonCode.sortOrder, asc);
                case "enabled" -> query.orderBy(asc ? commonCode.enabled.asc() : commonCode.enabled.desc());
                default -> applyNumberSort(query, commonCode.id, asc);
            }
        }
    }

    private <N extends Number & Comparable<?>> void applyNumberSort(
            JPAQuery<CommonCode> query,
            NumberPath<N> path,
            boolean ascending
    ) {
        query.orderBy(ascending ? path.asc() : path.desc());
    }
}
