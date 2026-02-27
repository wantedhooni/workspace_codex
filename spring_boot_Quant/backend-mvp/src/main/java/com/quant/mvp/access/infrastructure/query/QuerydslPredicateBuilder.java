package com.quant.mvp.access.infrastructure.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import java.time.Instant;
import java.util.Collection;

public final class QuerydslPredicateBuilder {

    private QuerydslPredicateBuilder() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static BooleanExpression eqIfPresent(StringPath path, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return path.equalsIgnoreCase(value.trim());
    }

    public static <T> BooleanExpression eqIfPresent(com.querydsl.core.types.dsl.SimpleExpression<T> path, T value) {
        if (value == null) {
            return null;
        }
        return path.eq(value);
    }

    public static BooleanExpression likeIfPresent(StringPath path, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return path.containsIgnoreCase(value.trim());
    }

    public static <T> BooleanExpression inIfNotEmpty(com.querydsl.core.types.dsl.SimpleExpression<T> path, Collection<T> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return path.in(values);
    }

    public static BooleanExpression betweenIfPresent(DateTimePath<Instant> path, Instant from, Instant to) {
        if (from != null && to != null) {
            return path.between(from, to);
        }
        if (from != null) {
            return path.goe(from);
        }
        if (to != null) {
            return path.loe(to);
        }
        return null;
    }

    public static final class Builder {

        private final BooleanBuilder delegate = new BooleanBuilder();

        private Builder() {
        }

        public Builder and(BooleanExpression expression) {
            if (expression != null) {
                delegate.and(expression);
            }
            return this;
        }

        public BooleanBuilder build() {
            return delegate;
        }
    }
}
