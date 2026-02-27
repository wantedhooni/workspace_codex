package com.curd.template.infra.filter;

import com.curd.template.core.filter.FilterOperator;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;

public interface FilterOperatorStrategy {

    FilterOperator operator();

    BooleanExpression build(PathBuilder<?> root, String field, Class<?> fieldType, String rawValue);
}
