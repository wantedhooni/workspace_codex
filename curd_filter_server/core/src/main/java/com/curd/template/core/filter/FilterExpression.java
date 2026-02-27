package com.curd.template.core.filter;

import java.util.List;

public record FilterExpression(List<FilterClause> andClauses, List<OrGroup> orGroups) {

    public FilterExpression {
        andClauses = andClauses == null ? List.of() : List.copyOf(andClauses);
        orGroups = orGroups == null ? List.of() : List.copyOf(orGroups);
    }

    public static FilterExpression empty() {
        return new FilterExpression(List.of(), List.of());
    }

    public boolean hasAnyClause() {
        return !andClauses.isEmpty() || !orGroups.isEmpty();
    }

    public int totalClauseCount() {
        int orCount = orGroups.stream().mapToInt(group -> group.clauses().size()).sum();
        return andClauses.size() + orCount;
    }
}
