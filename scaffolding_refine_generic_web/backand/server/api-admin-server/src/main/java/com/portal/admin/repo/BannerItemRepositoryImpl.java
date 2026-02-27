package com.portal.admin.repo;

import com.portal.admin.domain.BannerItem;
import com.portal.admin.domain.QBannerItem;
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

public class BannerItemRepositoryImpl implements SearchableRepository<BannerItem>, FieldSearchableRepository<BannerItem> {

    private final JPAQueryFactory queryFactory;

    public BannerItemRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<BannerItem> search(String query) {
        QBannerItem banner = QBannerItem.bannerItem;
        return queryFactory
                .selectFrom(banner)
                .where(
                        banner.title.containsIgnoreCase(query)
                                .or(banner.imageUrl.containsIgnoreCase(query))
                                .or(banner.linkUrl.containsIgnoreCase(query))
                )
                .orderBy(banner.sortOrder.asc(), banner.id.asc())
                .fetch();
    }

    @Override
    public Page<BannerItem> search(String query, Pageable pageable) {
        QBannerItem banner = QBannerItem.bannerItem;
        BooleanBuilder where = new BooleanBuilder()
                .and(
                        banner.title.containsIgnoreCase(query)
                                .or(banner.imageUrl.containsIgnoreCase(query))
                                .or(banner.linkUrl.containsIgnoreCase(query))
                );
        return fetchPage(banner, where, pageable);
    }

    @Override
    public List<BannerItem> searchByFields(Map<String, String> filters) {
        QBannerItem banner = QBannerItem.bannerItem;
        BooleanBuilder where = buildFieldWhere(banner, filters);

        return queryFactory
                .selectFrom(banner)
                .where(where)
                .orderBy(banner.sortOrder.asc(), banner.id.asc())
                .fetch();
    }

    @Override
    public Page<BannerItem> searchByFields(Map<String, String> filters, Pageable pageable) {
        QBannerItem banner = QBannerItem.bannerItem;
        BooleanBuilder where = buildFieldWhere(banner, filters);
        return fetchPage(banner, where, pageable);
    }

    @Override
    public Set<String> allowedSortFields() {
        return Set.of("id", "title", "enabled", "sortOrder", "startAt", "endAt");
    }

    @Override
    public Set<String> allowedFilterFields() {
        return Set.of("title", "imageUrl", "linkUrl", "startAt", "endAt", "enabled");
    }

    private BooleanBuilder buildFieldWhere(QBannerItem banner, Map<String, String> filters) {
        BooleanBuilder where = new BooleanBuilder();

        String title = filters.get("title");
        if (title != null && !title.isBlank()) {
            where.and(banner.title.containsIgnoreCase(title.trim()));
        }

        String imageUrl = filters.get("imageUrl");
        if (imageUrl != null && !imageUrl.isBlank()) {
            where.and(banner.imageUrl.containsIgnoreCase(imageUrl.trim()));
        }

        String linkUrl = filters.get("linkUrl");
        if (linkUrl != null && !linkUrl.isBlank()) {
            where.and(banner.linkUrl.containsIgnoreCase(linkUrl.trim()));
        }

        String startAt = filters.get("startAt");
        if (startAt != null && !startAt.isBlank()) {
            where.and(banner.startAt.containsIgnoreCase(startAt.trim()));
        }

        String endAt = filters.get("endAt");
        if (endAt != null && !endAt.isBlank()) {
            where.and(banner.endAt.containsIgnoreCase(endAt.trim()));
        }

        String enabled = filters.get("enabled");
        if (enabled != null && ("true".equalsIgnoreCase(enabled) || "false".equalsIgnoreCase(enabled))) {
            where.and(banner.enabled.eq(Boolean.parseBoolean(enabled)));
        }

        return where;
    }

    private Page<BannerItem> fetchPage(QBannerItem banner, BooleanBuilder where, Pageable pageable) {
        JPAQuery<BannerItem> query = queryFactory
                .selectFrom(banner)
                .where(where);

        applySort(query, pageable == null ? Sort.unsorted() : pageable.getSort(), banner);
        if (pageable != null && pageable.isPaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }
        List<BannerItem> content = query.fetch();

        Long total = queryFactory
                .select(banner.id.count())
                .from(banner)
                .where(where)
                .fetchOne();
        long totalCount = total == null ? 0L : total;
        Pageable resultPageable = pageable == null ? Pageable.unpaged() : pageable;
        return new PageImpl<>(content, resultPageable, totalCount);
    }

    private void applySort(JPAQuery<BannerItem> query, Sort sort, QBannerItem banner) {
        if (sort == null || sort.isUnsorted()) {
            query.orderBy(banner.sortOrder.asc(), banner.id.asc());
            return;
        }

        for (Sort.Order order : sort) {
            boolean asc = order.isAscending();
            switch (order.getProperty()) {
                case "id" -> applyNumberSort(query, banner.id, asc);
                case "title" -> query.orderBy(asc ? banner.title.asc() : banner.title.desc());
                case "enabled" -> query.orderBy(asc ? banner.enabled.asc() : banner.enabled.desc());
                case "sortOrder" -> applyNumberSort(query, banner.sortOrder, asc);
                case "startAt" -> query.orderBy(asc ? banner.startAt.asc() : banner.startAt.desc());
                case "endAt" -> query.orderBy(asc ? banner.endAt.asc() : banner.endAt.desc());
                default -> applyNumberSort(query, banner.id, asc);
            }
        }
    }

    private <N extends Number & Comparable<?>> void applyNumberSort(
            JPAQuery<BannerItem> query,
            NumberPath<N> path,
            boolean ascending
    ) {
        query.orderBy(ascending ? path.asc() : path.desc());
    }
}
