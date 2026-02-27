package com.quant.mvp.access.infrastructure.query;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;

public final class QuerydslSortMapper {

    private QuerydslSortMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static OrderSpecifier<?>[] map(Sort sort, Map<String, Expression<?>> whitelist) {
        if (sort == null || sort.isUnsorted()) {
            return new OrderSpecifier<?>[0];
        }

        List<OrderSpecifier<?>> result = new ArrayList<>();
        for (Sort.Order order : sort) {
            Expression<?> expression = whitelist.get(order.getProperty());
            if (expression == null) {
                throw new IllegalArgumentException("unsupported sort field: " + order.getProperty());
            }
            if (!(expression instanceof ComparableExpressionBase<?> comparable)) {
                throw new IllegalArgumentException("sort field is not comparable: " + order.getProperty());
            }

            result.add(new OrderSpecifier<>(
                    order.isAscending() ? Order.ASC : Order.DESC,
                    comparable
            ));
        }

        return result.toArray(new OrderSpecifier<?>[0]);
    }
}
