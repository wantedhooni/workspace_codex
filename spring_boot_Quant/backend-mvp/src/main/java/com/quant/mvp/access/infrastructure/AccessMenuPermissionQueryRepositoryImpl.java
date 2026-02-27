package com.quant.mvp.access.infrastructure;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quant.mvp.access.domain.AccessMenuPermission;
import com.quant.mvp.access.domain.QAccessMenu;
import com.quant.mvp.access.domain.QAccessMenuPermission;
import com.quant.mvp.access.domain.QAccessRole;
import com.quant.mvp.access.infrastructure.query.QuerydslPredicateBuilder;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AccessMenuPermissionQueryRepositoryImpl implements AccessMenuPermissionQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public AccessMenuPermissionQueryRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public List<AccessMenuPermission> searchMenuPermissions(Long roleId, Long menuId, String roleCode, String menuKey) {
        QAccessMenuPermission permission = QAccessMenuPermission.accessMenuPermission;
        QAccessRole role = QAccessRole.accessRole;
        QAccessMenu menu = QAccessMenu.accessMenu;

        BooleanBuilder where = QuerydslPredicateBuilder.builder()
                .and(QuerydslPredicateBuilder.eqIfPresent(permission.roleId, roleId))
                .and(QuerydslPredicateBuilder.eqIfPresent(permission.menuId, menuId))
                .and(QuerydslPredicateBuilder.likeIfPresent(role.roleCode, roleCode))
                .and(QuerydslPredicateBuilder.likeIfPresent(menu.menuKey, menuKey))
                .build();

        return jpaQueryFactory
                .select(permission)
                .from(permission)
                .join(role).on(role.id.eq(permission.roleId))
                .join(menu).on(menu.id.eq(permission.menuId))
                .where(where)
                .orderBy(permission.id.asc())
                .fetch();
    }
}
