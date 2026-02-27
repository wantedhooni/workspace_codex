package com.curd.template.core.filter;

import java.util.Map;
import java.util.Set;

public record FilterPolicy(
    Map<String, Set<FilterOperator>> allowedOperatorsByField,
    Set<String> sortableFields,
    int maxClauses,
    int maxValueLength,
    int maxPageSize
) {

    public FilterPolicy {
        allowedOperatorsByField = allowedOperatorsByField == null ? Map.of() : Map.copyOf(allowedOperatorsByField);
        sortableFields = sortableFields == null ? Set.of() : Set.copyOf(sortableFields);
        if (maxClauses <= 0) {
            throw new IllegalArgumentException("maxClauses must be positive");
        }
        if (maxValueLength <= 0) {
            throw new IllegalArgumentException("maxValueLength must be positive");
        }
        if (maxPageSize <= 0) {
            throw new IllegalArgumentException("maxPageSize must be positive");
        }
    }

    public boolean allows(String field, FilterOperator operator) {
        Set<FilterOperator> operators = allowedOperatorsByField.get(field);
        return operators != null && operators.contains(operator);
    }

    public boolean isSortable(String field) {
        return sortableFields.contains(field);
    }
}
