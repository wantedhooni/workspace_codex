package com.derivops.mvp.menu.infrastructure;
import com.derivops.mvp.menu.*;
import com.derivops.mvp.menu.api.*;
import com.derivops.mvp.menu.application.*;
import com.derivops.mvp.menu.dto.*;


import com.derivops.mvp.common.rsql.QuerydslRsqlPredicateBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class MenuEntryRepositoryImpl implements MenuEntryRepositoryCustom {

    private static final QMenuEntry menu = QMenuEntry.menuEntry;

    private static final Map<String, Expression<?>> FILTER_FIELDS = Map.ofEntries(
            Map.entry("id", menu.id),
            Map.entry("menuKey", menu.menuKey),
            Map.entry("title", menu.title),
            Map.entry("description", menu.description),
            Map.entry("path", menu.path),
            Map.entry("parentMenuKey", menu.parentMenuKey),
            Map.entry("resourceName", menu.resourceName),
            Map.entry("icon", menu.icon),
            Map.entry("sortOrder", menu.sortOrder),
            Map.entry("enabled", menu.enabled),
            Map.entry("rolesCsv", menu.rolesCsv),
            Map.entry("createdAt", menu.createdAt)
    );

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MenuEntry> search(String keyword, String filter, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.isBlank()) {
            String q = keyword.trim();
            builder.and(
                    menu.menuKey.containsIgnoreCase(q)
                            .or(menu.title.containsIgnoreCase(q))
                            .or(menu.description.containsIgnoreCase(q))
                            .or(menu.path.containsIgnoreCase(q))
                            .or(menu.parentMenuKey.containsIgnoreCase(q))
                            .or(menu.resourceName.containsIgnoreCase(q))
                            .or(menu.rolesCsv.containsIgnoreCase(q))
            );
        }
        if (filter != null && !filter.isBlank()) {
            builder.and(QuerydslRsqlPredicateBuilder.build(filter, FILTER_FIELDS));
        }

        List<MenuEntry> items = queryFactory
                .selectFrom(menu)
                .where(builder)
                .orderBy(menu.sortOrder.asc(), menu.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(menu.count())
                .from(menu)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(items, pageable, total == null ? 0L : total);
    }
}
