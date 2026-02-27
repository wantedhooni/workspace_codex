package com.quant.mvp.access.infrastructure;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quant.mvp.access.domain.AccessMenu;
import com.quant.mvp.access.domain.QAccessMenu;
import com.quant.mvp.access.infrastructure.query.QuerydslPredicateBuilder;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AccessMenuQueryRepositoryImpl implements AccessMenuQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public AccessMenuQueryRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public List<AccessMenu> searchMenus(Long menuId, Long parentMenuId, String menuKey, String menuLabel, Boolean enabled) {
        QAccessMenu menu = QAccessMenu.accessMenu;

        BooleanBuilder where = QuerydslPredicateBuilder.builder()
                .and(QuerydslPredicateBuilder.eqIfPresent(menu.id, menuId))
                .and(QuerydslPredicateBuilder.eqIfPresent(menu.parentMenuId, parentMenuId))
                .and(QuerydslPredicateBuilder.likeIfPresent(menu.menuKey, menuKey))
                .and(QuerydslPredicateBuilder.likeIfPresent(menu.menuLabel, menuLabel))
                .and(QuerydslPredicateBuilder.eqIfPresent(menu.enabled, enabled))
                .build();

        return jpaQueryFactory
                .selectFrom(menu)
                .where(where)
                .orderBy(menu.sortOrder.asc(), menu.id.asc())
                .fetch();
    }
}
