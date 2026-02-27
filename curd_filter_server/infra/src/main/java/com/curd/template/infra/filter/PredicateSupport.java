package com.curd.template.infra.filter;

import com.curd.template.core.error.ApiException;
import com.curd.template.core.error.AppErrorCode;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class PredicateSupport {

    private PredicateSupport() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static SimpleExpression<Object> path(PathBuilder<?> root, String field, Class<?> fieldType) {
        return (SimpleExpression<Object>) root.get(field, (Class) fieldType);
    }

    public static BooleanExpression equalsExpression(PathBuilder<?> root, String field, Class<?> fieldType, Object value) {
        return path(root, field, fieldType).eq(value);
    }

    public static BooleanExpression notEqualsExpression(PathBuilder<?> root, String field, Class<?> fieldType, Object value) {
        return path(root, field, fieldType).ne(value);
    }

    public static BooleanExpression isNull(PathBuilder<?> root, String field, Class<?> fieldType) {
        return path(root, field, fieldType).isNull();
    }

    public static BooleanExpression isNotNull(PathBuilder<?> root, String field, Class<?> fieldType) {
        return path(root, field, fieldType).isNotNull();
    }

    public static StringPath stringPath(PathBuilder<?> root, String field, Class<?> fieldType) {
        if (fieldType != String.class) {
            throw new ApiException(AppErrorCode.FILTER_POLICY_VIOLATION, "String operator used for non-string field: " + field);
        }
        return root.getString(field);
    }

    public static BooleanExpression inExpression(PathBuilder<?> root, String field, Class<?> fieldType, Iterable<?> values) {
        return path(root, field, fieldType).in(values);
    }

    public static BooleanExpression gt(PathBuilder<?> root, String field, Class<?> fieldType, Object value) {
        if (fieldType == BigDecimal.class) {
            return root.getNumber(field, BigDecimal.class).gt((BigDecimal) value);
        }
        if (fieldType == Integer.class || fieldType == int.class) {
            return root.getNumber(field, Integer.class).gt((Integer) value);
        }
        if (fieldType == Long.class || fieldType == long.class) {
            return root.getNumber(field, Long.class).gt((Long) value);
        }
        if (fieldType == Double.class || fieldType == double.class) {
            return root.getNumber(field, Double.class).gt((Double) value);
        }
        if (fieldType == Instant.class) {
            return root.getDateTime(field, Instant.class).gt((Instant) value);
        }
        if (fieldType == LocalDate.class) {
            return root.getDate(field, LocalDate.class).gt((LocalDate) value);
        }
        if (fieldType == LocalDateTime.class) {
            return root.getDateTime(field, LocalDateTime.class).gt((LocalDateTime) value);
        }
        throw unsupported(field);
    }

    public static BooleanExpression gte(PathBuilder<?> root, String field, Class<?> fieldType, Object value) {
        if (fieldType == BigDecimal.class) {
            return root.getNumber(field, BigDecimal.class).goe((BigDecimal) value);
        }
        if (fieldType == Integer.class || fieldType == int.class) {
            return root.getNumber(field, Integer.class).goe((Integer) value);
        }
        if (fieldType == Long.class || fieldType == long.class) {
            return root.getNumber(field, Long.class).goe((Long) value);
        }
        if (fieldType == Double.class || fieldType == double.class) {
            return root.getNumber(field, Double.class).goe((Double) value);
        }
        if (fieldType == Instant.class) {
            return root.getDateTime(field, Instant.class).goe((Instant) value);
        }
        if (fieldType == LocalDate.class) {
            return root.getDate(field, LocalDate.class).goe((LocalDate) value);
        }
        if (fieldType == LocalDateTime.class) {
            return root.getDateTime(field, LocalDateTime.class).goe((LocalDateTime) value);
        }
        throw unsupported(field);
    }

    public static BooleanExpression lt(PathBuilder<?> root, String field, Class<?> fieldType, Object value) {
        if (fieldType == BigDecimal.class) {
            return root.getNumber(field, BigDecimal.class).lt((BigDecimal) value);
        }
        if (fieldType == Integer.class || fieldType == int.class) {
            return root.getNumber(field, Integer.class).lt((Integer) value);
        }
        if (fieldType == Long.class || fieldType == long.class) {
            return root.getNumber(field, Long.class).lt((Long) value);
        }
        if (fieldType == Double.class || fieldType == double.class) {
            return root.getNumber(field, Double.class).lt((Double) value);
        }
        if (fieldType == Instant.class) {
            return root.getDateTime(field, Instant.class).lt((Instant) value);
        }
        if (fieldType == LocalDate.class) {
            return root.getDate(field, LocalDate.class).lt((LocalDate) value);
        }
        if (fieldType == LocalDateTime.class) {
            return root.getDateTime(field, LocalDateTime.class).lt((LocalDateTime) value);
        }
        throw unsupported(field);
    }

    public static BooleanExpression lte(PathBuilder<?> root, String field, Class<?> fieldType, Object value) {
        if (fieldType == BigDecimal.class) {
            return root.getNumber(field, BigDecimal.class).loe((BigDecimal) value);
        }
        if (fieldType == Integer.class || fieldType == int.class) {
            return root.getNumber(field, Integer.class).loe((Integer) value);
        }
        if (fieldType == Long.class || fieldType == long.class) {
            return root.getNumber(field, Long.class).loe((Long) value);
        }
        if (fieldType == Double.class || fieldType == double.class) {
            return root.getNumber(field, Double.class).loe((Double) value);
        }
        if (fieldType == Instant.class) {
            return root.getDateTime(field, Instant.class).loe((Instant) value);
        }
        if (fieldType == LocalDate.class) {
            return root.getDate(field, LocalDate.class).loe((LocalDate) value);
        }
        if (fieldType == LocalDateTime.class) {
            return root.getDateTime(field, LocalDateTime.class).loe((LocalDateTime) value);
        }
        throw unsupported(field);
    }

    public static BooleanExpression between(PathBuilder<?> root, String field, Class<?> fieldType, Object start, Object end) {
        return gte(root, field, fieldType, start).and(lte(root, field, fieldType, end));
    }

    private static ApiException unsupported(String field) {
        return new ApiException(AppErrorCode.FILTER_POLICY_VIOLATION, "Unsupported comparison type for field: " + field);
    }
}
