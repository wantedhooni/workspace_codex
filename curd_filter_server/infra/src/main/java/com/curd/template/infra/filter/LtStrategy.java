package com.curd.template.infra.filter;

import com.curd.template.core.filter.FilterOperator;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;

public class LtStrategy implements FilterOperatorStrategy {
    @Override
    public FilterOperator operator() {
        return FilterOperator.LT;
    }

    @Override
    public BooleanExpression build(PathBuilder<?> root, String field, Class<?> fieldType, String rawValue) {
        return PredicateSupport.lt(root, field, fieldType, ValueCoercion.convert(rawValue, fieldType));
    }
}
