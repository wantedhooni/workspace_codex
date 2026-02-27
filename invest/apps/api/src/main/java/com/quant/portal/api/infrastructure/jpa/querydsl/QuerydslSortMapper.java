package com.quant.portal.api.infrastructure.jpa.querydsl;

import com.quant.portal.api.application.exception.ApiException;
import com.quant.portal.api.application.exception.ErrorCode;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

public final class QuerydslSortMapper {

    private QuerydslSortMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static OrderSpecifier<?>[] toOrderSpecifiers(
            Sort sort,
            Map<String, ComparableExpressionBase<?>> whitelist
    ) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        for (Sort.Order order : sort) {
            ComparableExpressionBase<?> expression = whitelist.get(order.getProperty());
            if (expression == null) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.BAD_REQUEST,
                        "Unsupported sort field: " + order.getProperty()
                );
            }

            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            orderSpecifiers.add(new OrderSpecifier<>(direction, expression));
        }

        return orderSpecifiers.toArray(new OrderSpecifier<?>[0]);
    }
}
