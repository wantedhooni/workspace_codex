package com.quant.mvp.access.infrastructure;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quant.mvp.access.domain.AccessRole;
import com.quant.mvp.access.domain.QAccessRole;
import com.quant.mvp.access.infrastructure.query.QuerydslPredicateBuilder;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AccessRoleQueryRepositoryImpl implements AccessRoleQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public AccessRoleQueryRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public List<AccessRole> searchRoles(Long roleId, String roleCode, String roleName) {
        QAccessRole role = QAccessRole.accessRole;

        BooleanBuilder where = QuerydslPredicateBuilder.builder()
                .and(QuerydslPredicateBuilder.eqIfPresent(role.id, roleId))
                .and(QuerydslPredicateBuilder.likeIfPresent(role.roleCode, roleCode))
                .and(QuerydslPredicateBuilder.likeIfPresent(role.roleName, roleName))
                .build();

        return jpaQueryFactory
                .selectFrom(role)
                .where(where)
                .orderBy(role.id.asc())
                .fetch();
    }
}
