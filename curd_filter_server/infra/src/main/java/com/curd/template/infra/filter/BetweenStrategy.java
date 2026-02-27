package com.curd.template.infra.filter;

import com.curd.template.core.error.ApiException;
import com.curd.template.core.error.AppErrorCode;
import com.curd.template.core.filter.FilterOperator;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;

public class BetweenStrategy implements FilterOperatorStrategy {

    @Override
    public FilterOperator operator() {
        return FilterOperator.BETWEEN;
    }

    @Override
    public BooleanExpression build(PathBuilder<?> root, String field, Class<?> fieldType, String rawValue) {
        String[] bounds = rawValue.split(",", 2);
        if (bounds.length != 2) {
            throw new ApiException(AppErrorCode.FILTER_PARSE_ERROR, "between operator requires two values separated by comma");
        }
        Object start = ValueCoercion.convert(bounds[0].trim(), fieldType);
        Object end = ValueCoercion.convert(bounds[1].trim(), fieldType);
        return PredicateSupport.between(root, field, fieldType, start, end);
    }
}
