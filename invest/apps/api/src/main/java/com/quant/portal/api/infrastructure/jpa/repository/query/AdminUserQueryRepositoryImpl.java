package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quant.portal.api.application.query.AdminUserSearchCondition;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslPredicateBuilder;
import com.quant.portal.api.infrastructure.jpa.querydsl.QuerydslSortMapper;
import com.quant.portal.domain.admin.entity.AdminUser;
import com.quant.portal.domain.admin.entity.QAdminUser;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
public class AdminUserQueryRepositoryImpl implements AdminUserQueryRepository {

    private final JPAQueryFactory queryFactory;

    public AdminUserQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<AdminUser> search(AdminUserSearchCondition condition, Pageable pageable) {
        QAdminUser adminUser = QAdminUser.adminUser;

        BooleanBuilder where = new BooleanBuilder();
        if (condition.keyword() != null && !condition.keyword().isBlank()) {
            where.and(
                    QuerydslPredicateBuilder.likeIfPresent(adminUser.username, condition.keyword())
                            .or(QuerydslPredicateBuilder.likeIfPresent(adminUser.displayName, condition.keyword()))
            );
        }
        if (condition.roleCode() != null && !condition.roleCode().isBlank()) {
            where.and(adminUser.roleCodes.any().eq(condition.roleCode()));
        }

        List<AdminUser> content = queryFactory
                .selectFrom(adminUser)
                .distinct()
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QuerydslSortMapper.toOrderSpecifiers(pageable.getSort(), sortWhitelist(adminUser)))
                .fetch();

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> {
                    Long count = queryFactory
                            .select(adminUser.id.countDistinct())
                            .from(adminUser)
                            .where(where)
                            .fetchOne();
                    return count == null ? 0L : count;
                }
        );
    }

    @Override
    public long countByRoleCode(String roleCode) {
        QAdminUser adminUser = QAdminUser.adminUser;
        Long count = queryFactory
                .select(adminUser.id.countDistinct())
                .from(adminUser)
                .where(adminUser.roleCodes.any().eq(roleCode))
                .fetchOne();
        return count == null ? 0L : count;
    }

    private Map<String, ComparableExpressionBase<?>> sortWhitelist(QAdminUser adminUser) {
        return Map.of(
                "id", adminUser.id,
                "username", adminUser.username,
                "displayName", adminUser.displayName,
                "createdAt", adminUser.createdAt
        );
    }
}

