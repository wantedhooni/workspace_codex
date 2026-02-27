package com.quant.portal.api.infrastructure.jpa.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import java.time.LocalDate;
import java.util.Collection;

public final class QuerydslPredicateBuilder {

    private QuerydslPredicateBuilder() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static <T> BooleanExpression eqIfPresent(SimpleExpression<T> path, T value) {
        if (value == null) {
            return null;
        }
        return path.eq(value);
    }

    public static BooleanExpression likeIfPresent(StringExpression path, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return path.containsIgnoreCase(keyword.trim());
    }

    public static BooleanExpression betweenIfPresent(DatePath<LocalDate> path, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return null;
        }
        if (from != null && to != null) {
            return path.between(from, to);
        }
        if (from != null) {
            return path.goe(from);
        }
        return path.loe(to);
    }

    public static <T> BooleanExpression inIfNotEmpty(SimpleExpression<T> path, Collection<T> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return path.in(values);
    }
}
