package com.curd.template.core.filter;

public record FilterClause(String field, FilterOperator operator, String value) {
    public FilterClause {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("field must not be blank");
        }
        if (operator == null) {
            throw new IllegalArgumentException("operator must not be null");
        }
    }
}
