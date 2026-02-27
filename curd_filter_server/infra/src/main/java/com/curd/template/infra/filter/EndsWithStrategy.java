package com.curd.template.infra.filter;

import com.curd.template.core.filter.FilterOperator;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;

public class EndsWithStrategy implements FilterOperatorStrategy {

    @Override
    public FilterOperator operator() {
        return FilterOperator.ENDS_WITH;
    }

    @Override
    public BooleanExpression build(PathBuilder<?> root, String field, Class<?> fieldType, String rawValue) {
        return PredicateSupport.stringPath(root, field, fieldType).endsWithIgnoreCase(rawValue);
    }
}
