package com.curd.template.infra.filter;

import com.curd.template.core.filter.FilterOperator;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import java.util.Arrays;
import java.util.List;

public class InStrategy implements FilterOperatorStrategy {

    @Override
    public FilterOperator operator() {
        return FilterOperator.IN;
    }

    @Override
    public BooleanExpression build(PathBuilder<?> root, String field, Class<?> fieldType, String rawValue) {
        List<Object> values = Arrays.stream(rawValue.split(","))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .map(value -> ValueCoercion.convert(value, fieldType))
            .toList();
        return PredicateSupport.inExpression(root, field, fieldType, values);
    }
}
