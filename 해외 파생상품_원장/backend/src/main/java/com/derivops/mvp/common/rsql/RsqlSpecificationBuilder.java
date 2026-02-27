package com.derivops.mvp.common.rsql;

import com.derivops.mvp.common.BadRequestException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class RsqlSpecificationBuilder {

    private RsqlSpecificationBuilder() {
    }

    public static <T> Specification<T> build(String filter, Map<String, String> selectorToPath) {
        List<List<RsqlExpression>> groups = RsqlParser.parseToOrAndGroups(filter);
        if (groups.isEmpty()) {
            return Specification.where(null);
        }

        return (root, query, cb) -> {
            List<Predicate> orPredicates = new ArrayList<>();
            for (List<RsqlExpression> andGroup : groups) {
                List<Predicate> andPredicates = new ArrayList<>();
                for (RsqlExpression expression : andGroup) {
                    String pathName = selectorToPath.get(expression.selector());
                    if (pathName == null) {
                        throw new BadRequestException("Unsupported RSQL field: " + expression.selector());
                    }
                    Path<?> path = resolvePath(root, pathName);
                    andPredicates.add(buildPredicate(cb, path, expression.operator(), expression.arguments()));
                }
                orPredicates.add(cb.and(andPredicates.toArray(Predicate[]::new)));
            }
            return cb.or(orPredicates.toArray(Predicate[]::new));
        };
    }

    private static Path<?> resolvePath(Root<?> root, String path) {
        String[] segments = path.split("\\.");
        Path<?> current = root;
        for (String segment : segments) {
            current = current.get(segment);
        }
        return current;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Predicate buildPredicate(CriteriaBuilder cb, Path<?> path, String operator, List<String> arguments) {
        Class<?> type = path.getJavaType();

        return switch (operator) {
            case "==" -> cb.equal(path, convert(arguments.get(0), type));
            case "!=" -> cb.notEqual(path, convert(arguments.get(0), type));
            case "=like=" -> cb.like(cb.lower(path.as(String.class)), "%" + arguments.get(0).toLowerCase() + "%");
            case "=gt=" -> cb.greaterThan((Expression<? extends Comparable>) path, (Comparable) convert(arguments.get(0), type));
            case "=ge=" -> cb.greaterThanOrEqualTo((Expression<? extends Comparable>) path, (Comparable) convert(arguments.get(0), type));
            case "=lt=" -> cb.lessThan((Expression<? extends Comparable>) path, (Comparable) convert(arguments.get(0), type));
            case "=le=" -> cb.lessThanOrEqualTo((Expression<? extends Comparable>) path, (Comparable) convert(arguments.get(0), type));
            case "=in=" -> path.in(arguments.stream().map(arg -> convert(arg, type)).toList());
            case "=out=" -> cb.not(path.in(arguments.stream().map(arg -> convert(arg, type)).toList()));
            default -> throw new BadRequestException("Unsupported RSQL operator: " + operator);
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
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
}
