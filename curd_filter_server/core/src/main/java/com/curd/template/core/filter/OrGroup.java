package com.curd.template.core.filter;

import java.util.List;

public record OrGroup(List<FilterClause> clauses) {
    public OrGroup {
        if (clauses == null || clauses.isEmpty()) {
            throw new IllegalArgumentException("or group must contain at least one clause");
        }
    }
}
