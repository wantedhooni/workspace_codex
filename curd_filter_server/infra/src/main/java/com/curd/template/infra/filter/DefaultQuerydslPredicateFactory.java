package com.curd.template.infra.filter;

import com.curd.template.core.error.ApiException;
import com.curd.template.core.error.AppErrorCode;
import com.curd.template.core.filter.FilterClause;
import com.curd.template.core.filter.FilterExpression;
import com.curd.template.core.filter.FilterOperator;
import com.curd.template.core.filter.OrGroup;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class DefaultQuerydslPredicateFactory implements QuerydslPredicateFactory {

    private final PathBuilder<?> pathBuilder;
    private final Map<String, Class<?>> fieldTypes;
    private final Map<FilterOperator, FilterOperatorStrategy> strategies;

    public DefaultQuerydslPredicateFactory(
        Class<?> entityType,
        String variable,
        Map<String, Class<?>> fieldTypes,
        List<FilterOperatorStrategy> strategies
    ) {
        this.pathBuilder = new PathBuilder<>(entityType, variable);
        this.fieldTypes = Map.copyOf(fieldTypes);
        this.strategies = toStrategyMap(strategies);
    }

    @Override
    public Predicate toPredicate(FilterExpression expression) {
        if (expression == null || !expression.hasAnyClause()) {
            return Expressions.TRUE.isTrue();
        }

        BooleanBuilder rootBuilder = new BooleanBuilder();
        for (FilterClause andClause : expression.andClauses()) {
            rootBuilder.and(toExpression(andClause));
        }

        for (OrGroup orGroup : expression.orGroups()) {
            BooleanBuilder groupBuilder = new BooleanBuilder();
            for (FilterClause clause : orGroup.clauses()) {
                groupBuilder.or(toExpression(clause));
            }
            rootBuilder.and(groupBuilder);
        }

        Predicate predicate = rootBuilder.getValue();
        return predicate == null ? Expressions.TRUE.isTrue() : predicate;
    }

    private BooleanExpression toExpression(FilterClause clause) {
        Class<?> fieldType = fieldTypes.get(clause.field());
        if (fieldType == null) {
            throw new ApiException(AppErrorCode.FILTER_POLICY_VIOLATION, "Field type metadata missing: " + clause.field());
        }

        FilterOperatorStrategy strategy = strategies.get(clause.operator());
        if (strategy == null) {
            throw new ApiException(AppErrorCode.FILTER_POLICY_VIOLATION, "Operator strategy missing: " + clause.operator());
        }

        return strategy.build(pathBuilder, clause.field(), fieldType, clause.value());
    }

    private Map<FilterOperator, FilterOperatorStrategy> toStrategyMap(List<FilterOperatorStrategy> values) {
        Map<FilterOperator, FilterOperatorStrategy> map = new EnumMap<>(FilterOperator.class);
        for (FilterOperatorStrategy strategy : values) {
            map.put(strategy.operator(), strategy);
        }
        return map;
    }
}
