package com.derivops.mvp.common.rsql;

import com.derivops.mvp.common.BadRequestException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class QuerydslRsqlPredicateBuilder {

    private QuerydslRsqlPredicateBuilder() {
    }

    public static BooleanBuilder build(String filter, Map<String, Expression<?>> selectorMap) {
        List<List<RsqlExpression>> groups = RsqlParser.parseToOrAndGroups(filter);
        BooleanBuilder root = new BooleanBuilder();
        if (groups.isEmpty()) {
            return root;
        }

        boolean initialized = false;
        for (List<RsqlExpression> andGroup : groups) {
            BooleanBuilder andBuilder = new BooleanBuilder();
            for (RsqlExpression expression : andGroup) {
                Expression<?> target = selectorMap.get(expression.selector());
                if (target == null) {
                    throw new BadRequestException("Unsupported RSQL field: " + expression.selector());
                }
                andBuilder.and(buildExpressionPredicate(target, expression));
            }

            if (!initialized) {
                root.and(andBuilder);
                initialized = true;
            } else {
                root.or(andBuilder);
            }
        }

        return root;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Predicate buildExpressionPredicate(Expression<?> expression, RsqlExpression rsqlExpression) {
        RsqlOperator operator = rsqlExpression.operator();
        Class<?> type = expression.getType();

        if (operator == RsqlOperator.LIKE) {
            if (!(expression instanceof StringExpression str)) {
                throw new BadRequestException("=like= is only supported for string fields");
            }
            return str.containsIgnoreCase(singleValue(rsqlExpression.argument()));
        }

        if (operator == RsqlOperator.IN || operator == RsqlOperator.NOT_IN) {
            if (!(expression instanceof SimpleExpression simple)) {
                throw new BadRequestException("IN/OUT operator is unsupported for field type: " + type.getSimpleName());
            }
            List<Object> converted = new ArrayList<>();
            for (String argument : multiValue(rsqlExpression.argument())) {
                converted.add(convert(argument, type));
            }
            Predicate inPredicate = simple.in(converted);
            return operator == RsqlOperator.IN ? inPredicate : inPredicate.not();
        }

        Object value = convert(singleValue(rsqlExpression.argument()), type);

        if (operator == RsqlOperator.EQUAL || operator == RsqlOperator.NOT_EQUAL) {
            if (!(expression instanceof SimpleExpression simple)) {
                throw new BadRequestException("Equality operator is unsupported for field type: " + type.getSimpleName());
            }
            Predicate predicate = simple.eq(value);
            return operator == RsqlOperator.EQUAL ? predicate : predicate.not();
        }

        if (!(expression instanceof ComparableExpressionBase<?>)) {
            throw new BadRequestException("Comparison operator is unsupported for field type: " + type.getSimpleName());
        }

        ComparableExpression comparable = (ComparableExpression) expression;
        Comparable comparableValue = (Comparable) value;
        return switch (operator) {
            case GREATER_THAN -> comparable.gt(comparableValue);
            case GREATER_THAN_OR_EQUAL -> comparable.goe(comparableValue);
            case LESS_THAN -> comparable.lt(comparableValue);
            case LESS_THAN_OR_EQUAL -> comparable.loe(comparableValue);
            default -> throw new BadRequestException("Unsupported RSQL operator: " + operator);
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object convert(String raw, Class<?> type) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();

        if (String.class.equals(type)) {
            return value;
        }
        if (Long.class.equals(type) || long.class.equals(type)) {
            return Long.parseLong(value);
        }
        if (Integer.class.equals(type) || int.class.equals(type)) {
            return Integer.parseInt(value);
        }
        if (Boolean.class.equals(type) || boolean.class.equals(type)) {
            return Boolean.parseBoolean(value);
        }
        if (BigDecimal.class.equals(type)) {
            return new BigDecimal(value);
        }
        if (UUID.class.equals(type)) {
            return UUID.fromString(value);
        }
        if (LocalDate.class.equals(type)) {
            return LocalDate.parse(value);
        }
        if (OffsetDateTime.class.equals(type)) {
            return OffsetDateTime.parse(value);
        }
        if (Enum.class.isAssignableFrom(type)) {
            return Enum.valueOf((Class<? extends Enum>) type, value);
        }

        throw new BadRequestException("Unsupported RSQL value type: " + type.getSimpleName());
    }

    private static String singleValue(RsqlArgument argument) {
        if (argument instanceof RsqlArgument.SingleValue singleValue) {
            return singleValue.value();
        }
        throw new BadRequestException("Single-value RSQL operator received multiple arguments");
    }

    private static List<String> multiValue(RsqlArgument argument) {
        if (argument instanceof RsqlArgument.MultiValue multiValue) {
            return multiValue.values();
        }
        throw new BadRequestException("Multi-value RSQL operator received a single argument");
    }
}
