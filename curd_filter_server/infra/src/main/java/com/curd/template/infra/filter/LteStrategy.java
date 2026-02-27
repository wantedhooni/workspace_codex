package com.curd.template.infra.filter;

import com.curd.template.core.filter.FilterOperator;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;

public class LteStrategy implements FilterOperatorStrategy {
    @Override
    public FilterOperator operator() {
        return FilterOperator.LTE;
    }

    @Override
    public BooleanExpression build(PathBuilder<?> root, String field, Class<?> fieldType, String rawValue) {
        return PredicateSupport.lte(root, field, fieldType, ValueCoercion.convert(rawValue, fieldType));
    }
}
