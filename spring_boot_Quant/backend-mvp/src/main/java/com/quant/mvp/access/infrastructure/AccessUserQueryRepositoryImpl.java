package com.quant.mvp.access.infrastructure;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quant.mvp.access.domain.AccessUser;
import com.quant.mvp.access.domain.QAccessRole;
import com.quant.mvp.access.domain.QAccessUser;
import com.quant.mvp.access.domain.QAccessUserRole;
import com.quant.mvp.access.infrastructure.query.QuerydslPredicateBuilder;
import com.quant.mvp.pipeline.domain.UserStatus;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AccessUserQueryRepositoryImpl implements AccessUserQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public AccessUserQueryRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public List<AccessUser> searchUsers(Long userId, String email, String name, UserStatus status, String roleCode) {
        QAccessUser user = QAccessUser.accessUser;
        QAccessUserRole userRole = QAccessUserRole.accessUserRole;
        QAccessRole role = QAccessRole.accessRole;

        BooleanBuilder where = QuerydslPredicateBuilder.builder()
                .and(QuerydslPredicateBuilder.eqIfPresent(user.id, userId))
                .and(QuerydslPredicateBuilder.likeIfPresent(user.email, email))
                .and(QuerydslPredicateBuilder.likeIfPresent(user.name, name))
                .and(QuerydslPredicateBuilder.eqIfPresent(user.status, status))
                .and(QuerydslPredicateBuilder.likeIfPresent(role.roleCode, roleCode))
                .build();

        return jpaQueryFactory
                .select(user)
                .from(user)
                .leftJoin(userRole).on(userRole.userId.eq(user.id))
                .leftJoin(role).on(role.id.eq(userRole.roleId))
                .where(where)
                .orderBy(user.id.asc())
                .distinct()
                .fetch();
    }
}
