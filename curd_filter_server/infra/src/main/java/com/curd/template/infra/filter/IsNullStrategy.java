package com.curd.template.infra.filter;

import com.curd.template.core.filter.FilterOperator;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;

public class IsNullStrategy implements FilterOperatorStrategy {

    @Override
    public FilterOperator operator() {
        return FilterOperator.IS_NULL;
    }

    @Override
    public BooleanExpression build(PathBuilder<?> root, String field, Class<?> fieldType, String rawValue) {
        return PredicateSupport.isNull(root, field, fieldType);
    }
}
