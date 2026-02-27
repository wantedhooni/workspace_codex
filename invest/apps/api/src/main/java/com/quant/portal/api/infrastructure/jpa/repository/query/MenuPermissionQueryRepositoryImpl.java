package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quant.portal.api.application.query.MenuPermissionSearchCondition;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslPredicateBuilder;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslSortMapper;
import com.quant.portal.domain.admin.entity.MenuPermission;
import com.quant.portal.domain.admin.entity.QMenuPermission;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
public class MenuPermissionQueryRepositoryImpl implements MenuPermissionQueryRepository {

    private final JPAQueryFactory queryFactory;

    public MenuPermissionQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<MenuPermission> search(MenuPermissionSearchCondition condition, Pageable pageable) {
        QMenuPermission menuPermission = QMenuPermission.menuPermission;

        BooleanBuilder where = new BooleanBuilder();
        where.and(QuerydslPredicateBuilder.likeIfPresent(menuPermission.menuKey, condition.keyword()));
        where.and(QuerydslPredicateBuilder.eqIfPresent(menuPermission.roleCode, condition.roleCode()));

        List<MenuPermission> content = queryFactory
                .selectFrom(menuPermission)
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QuerydslSortMapper.toOrderSpecifiers(pageable.getSort(), sortWhitelist(menuPermission)))
                .fetch();

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> {
                    Long count = queryFactory
                            .select(menuPermission.count())
                            .from(menuPermission)
                            .where(where)
                            .fetchOne();
                    return count == null ? 0L : count;
                }
        );
    }

    private Map<String, ComparableExpressionBase<?>> sortWhitelist(QMenuPermission menuPermission) {
        return Map.of(
                "id", menuPermission.id,
                "roleCode", menuPermission.roleCode,
                "menuKey", menuPermission.menuKey,
                "createdAt", menuPermission.createdAt
        );
    }
}
