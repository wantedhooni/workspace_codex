package com.revy.scaffolding.user.domain.repository;

import static com.revy.scaffolding.user.domain.QUser.user;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.scaffolding.user.dto.UserSummaryResponse;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class UserQueryRepositoryImpl implements UserQueryRepository {
    private final JPAQueryFactory queryFactory;

    public UserQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<UserSummaryResponse> search(String keyword) {
        return queryFactory
            .selectFrom(user)
            .where(containsKeyword(keyword))
            .orderBy(user.id.desc())
            .fetch()
            .stream()
            .map(UserSummaryResponse::from)
            .toList();
    }

    private BooleanExpression containsKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return user.name.containsIgnoreCase(keyword).or(user.email.containsIgnoreCase(keyword));
    }
}

