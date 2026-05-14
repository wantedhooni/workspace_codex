package com.example.websample.domain.post;

import static com.example.websample.domain.post.QPost.post;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/** Querydsl로 게시글 검색 조건을 동적으로 조립하는 저장소 구현체입니다. */
@Repository
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public PostRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<Post> search(PostSearchCondition condition, Pageable pageable) {
        List<Post> content = queryFactory
                .selectFrom(post)
                .join(post.author).fetchJoin()
                .where(keywordContains(condition.keyword()), authorIdEq(condition.authorId()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(defaultOrder())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .where(keywordContains(condition.keyword()), authorIdEq(condition.authorId()));

        Long total = countQuery.fetchOne();
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return post.title.containsIgnoreCase(keyword)
                .or(post.content.containsIgnoreCase(keyword));
    }

    private BooleanExpression authorIdEq(Long authorId) {
        return authorId == null ? null : post.author.id.eq(authorId);
    }

    private OrderSpecifier<?> defaultOrder() {
        return post.createdAt.desc();
    }
}
