package com.curd.template.infra.filter;

import com.curd.template.core.filter.FilterOperator;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;

public class NeStrategy implements FilterOperatorStrategy {

    @Override
    public FilterOperator operator() {
        return FilterOperator.NE;
    }

    @Override
    public BooleanExpression build(PathBuilder<?> root, String field, Class<?> fieldType, String rawValue) {
        Object value = ValueCoercion.convert(rawValue, fieldType);
        return PredicateSupport.notEqualsExpression(root, field, fieldType, value);
    }
}
