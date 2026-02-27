package com.portal.admin.menu.repo;

import com.portal.admin.menu.domain.MenuItem;
import com.portal.admin.menu.domain.QMenuItem;
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

public class MenuRepositoryImpl implements SearchableRepository<MenuItem>, FieldSearchableRepository<MenuItem> {

    private final JPAQueryFactory queryFactory;

    public MenuRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<MenuItem> search(String query) {
        QMenuItem menu = QMenuItem.menuItem;
        return queryFactory
                .selectFrom(menu)
                .where(
                        menu.title.containsIgnoreCase(query)
                                .or(menu.path.containsIgnoreCase(query))
                )
                .orderBy(menu.sortOrder.asc(), menu.id.asc())
                .fetch();
    }

    @Override
    public Page<MenuItem> search(String query, Pageable pageable) {
        QMenuItem menu = QMenuItem.menuItem;
        BooleanBuilder where = new BooleanBuilder()
                .and(
                        menu.title.containsIgnoreCase(query)
                                .or(menu.path.containsIgnoreCase(query))
                );
        return fetchPage(menu, where, pageable);
    }

    @Override
    public List<MenuItem> searchByFields(Map<String, String> filters) {
        QMenuItem menu = QMenuItem.menuItem;
        BooleanBuilder where = buildFieldWhere(menu, filters);

        return queryFactory
                .selectFrom(menu)
                .where(where)
                .orderBy(menu.sortOrder.asc(), menu.id.asc())
                .fetch();
    }

    @Override
    public Page<MenuItem> searchByFields(Map<String, String> filters, Pageable pageable) {
        QMenuItem menu = QMenuItem.menuItem;
        BooleanBuilder where = buildFieldWhere(menu, filters);
        return fetchPage(menu, where, pageable);
    }

    @Override
    public Set<String> allowedSortFields() {
        return Set.of("id", "title", "path", "parentId", "sortOrder");
    }

    @Override
    public Set<String> allowedFilterFields() {
        return Set.of("title", "path", "parentId", "sortOrder");
    }

    private BooleanBuilder buildFieldWhere(QMenuItem menu, Map<String, String> filters) {
        BooleanBuilder where = new BooleanBuilder();

        String title = filters.get("title");
        if (title != null && !title.isBlank()) {
            where.and(menu.title.containsIgnoreCase(title.trim()));
        }

        String path = filters.get("path");
        if (path != null && !path.isBlank()) {
            where.and(menu.path.containsIgnoreCase(path.trim()));
        }

        String parentId = filters.get("parentId");
        if (parentId != null) {
            try {
                where.and(menu.parentId.eq(Long.parseLong(parentId.trim())));
            } catch (NumberFormatException ignored) {
            }
        }

        String sortOrder = filters.get("sortOrder");
        if (sortOrder != null) {
            try {
                where.and(menu.sortOrder.eq(Integer.parseInt(sortOrder.trim())));
            } catch (NumberFormatException ignored) {
            }
        }

        return where;
    }

    private Page<MenuItem> fetchPage(QMenuItem menu, BooleanBuilder where, Pageable pageable) {
        JPAQuery<MenuItem> query = queryFactory
                .selectFrom(menu)
                .where(where);

        applySort(query, pageable == null ? Sort.unsorted() : pageable.getSort(), menu);
        if (pageable != null && pageable.isPaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }
        List<MenuItem> content = query.fetch();

        Long total = queryFactory
                .select(menu.id.count())
                .from(menu)
                .where(where)
                .fetchOne();
        long totalCount = total == null ? 0L : total;
        Pageable resultPageable = pageable == null ? Pageable.unpaged() : pageable;
        return new PageImpl<>(content, resultPageable, totalCount);
    }

    private void applySort(JPAQuery<MenuItem> query, Sort sort, QMenuItem menu) {
        if (sort == null || sort.isUnsorted()) {
            query.orderBy(menu.sortOrder.asc(), menu.id.asc());
            return;
        }

        for (Sort.Order order : sort) {
            boolean asc = order.isAscending();
            switch (order.getProperty()) {
                case "id" -> applyNumberSort(query, menu.id, asc);
                case "title" -> query.orderBy(asc ? menu.title.asc() : menu.title.desc());
                case "path" -> query.orderBy(asc ? menu.path.asc() : menu.path.desc());
                case "parentId" -> applyNumberSort(query, menu.parentId, asc);
                case "sortOrder" -> applyNumberSort(query, menu.sortOrder, asc);
                default -> applyNumberSort(query, menu.id, asc);
            }
        }
    }

    private <N extends Number & Comparable<?>> void applyNumberSort(
            JPAQuery<MenuItem> query,
            NumberPath<N> path,
            boolean ascending
    ) {
        query.orderBy(ascending ? path.asc() : path.desc());
    }
}
