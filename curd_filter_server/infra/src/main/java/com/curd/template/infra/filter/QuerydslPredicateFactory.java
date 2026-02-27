package com.curd.template.infra.filter;

import com.curd.template.core.filter.FilterExpression;
import com.querydsl.core.types.Predicate;

public interface QuerydslPredicateFactory {
    Predicate toPredicate(FilterExpression expression);
}
