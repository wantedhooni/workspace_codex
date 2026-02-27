package com.portal.admin.repo;

import com.portal.admin.domain.ContentPage;
import com.portal.admin.domain.QContentPage;
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

public class ContentRepositoryImpl implements SearchableRepository<ContentPage>, FieldSearchableRepository<ContentPage> {

    private final JPAQueryFactory queryFactory;

    public ContentRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<ContentPage> search(String query) {
        QContentPage content = QContentPage.contentPage;
        return queryFactory
                .selectFrom(content)
                .where(
                        content.title.containsIgnoreCase(query)
                                .or(content.slug.containsIgnoreCase(query))
                                .or(content.body.containsIgnoreCase(query))
                                .or(content.status.containsIgnoreCase(query))
                )
                .orderBy(content.id.asc())
                .fetch();
    }

    @Override
    public Page<ContentPage> search(String query, Pageable pageable) {
        QContentPage content = QContentPage.contentPage;
        BooleanBuilder where = new BooleanBuilder()
                .and(
                        content.title.containsIgnoreCase(query)
                                .or(content.slug.containsIgnoreCase(query))
                                .or(content.body.containsIgnoreCase(query))
                                .or(content.status.containsIgnoreCase(query))
                );
        return fetchPage(content, where, pageable);
    }

    @Override
    public List<ContentPage> searchByFields(Map<String, String> filters) {
        QContentPage content = QContentPage.contentPage;
        BooleanBuilder where = buildFieldWhere(content, filters);

        return queryFactory
                .selectFrom(content)
                .where(where)
                .orderBy(content.id.asc())
                .fetch();
    }

    @Override
    public Page<ContentPage> searchByFields(Map<String, String> filters, Pageable pageable) {
        QContentPage content = QContentPage.contentPage;
        BooleanBuilder where = buildFieldWhere(content, filters);
        return fetchPage(content, where, pageable);
    }

    @Override
    public Set<String> allowedSortFields() {
        return Set.of("id", "title", "slug", "status", "updatedAt");
    }

    @Override
    public Set<String> allowedFilterFields() {
        return Set.of("title", "slug", "body", "status");
    }

    private BooleanBuilder buildFieldWhere(QContentPage content, Map<String, String> filters) {
        BooleanBuilder where = new BooleanBuilder();

        String title = filters.get("title");
        if (title != null && !title.isBlank()) {
            where.and(content.title.containsIgnoreCase(title.trim()));
        }

        String slug = filters.get("slug");
        if (slug != null && !slug.isBlank()) {
            where.and(content.slug.containsIgnoreCase(slug.trim()));
        }

        String body = filters.get("body");
        if (body != null && !body.isBlank()) {
            where.and(content.body.containsIgnoreCase(body.trim()));
        }

        String status = filters.get("status");
        if (status != null && !status.isBlank()) {
            where.and(content.status.eq(status.trim()));
        }

        return where;
    }

    private Page<ContentPage> fetchPage(QContentPage content, BooleanBuilder where, Pageable pageable) {
        JPAQuery<ContentPage> query = queryFactory
                .selectFrom(content)
                .where(where);

        applySort(query, pageable == null ? Sort.unsorted() : pageable.getSort(), content);
        if (pageable != null && pageable.isPaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }
        List<ContentPage> pageContent = query.fetch();

        Long total = queryFactory
                .select(content.id.count())
                .from(content)
                .where(where)
                .fetchOne();
        long totalCount = total == null ? 0L : total;
        Pageable resultPageable = pageable == null ? Pageable.unpaged() : pageable;
        return new PageImpl<>(pageContent, resultPageable, totalCount);
    }

    private void applySort(JPAQuery<ContentPage> query, Sort sort, QContentPage content) {
        if (sort == null || sort.isUnsorted()) {
            query.orderBy(content.id.asc());
            return;
        }

        for (Sort.Order order : sort) {
            boolean asc = order.isAscending();
            switch (order.getProperty()) {
                case "id" -> applyNumberSort(query, content.id, asc);
                case "title" -> query.orderBy(asc ? content.title.asc() : content.title.desc());
                case "slug" -> query.orderBy(asc ? content.slug.asc() : content.slug.desc());
                case "status" -> query.orderBy(asc ? content.status.asc() : content.status.desc());
                case "updatedAt" -> query.orderBy(asc ? content.updatedAt.asc() : content.updatedAt.desc());
                default -> applyNumberSort(query, content.id, asc);
            }
        }
    }

    private <N extends Number & Comparable<?>> void applyNumberSort(
            JPAQuery<ContentPage> query,
            NumberPath<N> path,
            boolean ascending
    ) {
        query.orderBy(ascending ? path.asc() : path.desc());
    }
}
