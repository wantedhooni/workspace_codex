package com.curd.template.core.filter;

import java.util.Arrays;

public enum FilterOperator {
    EQ("eq"),
    NE("ne"),
    IS_NULL("isNull"),
    IS_NOT_NULL("isNotNull"),
    IN("in"),
    CONTAINS("contains"),
    STARTS_WITH("startsWith"),
    ENDS_WITH("endsWith"),
    GT("gt"),
    GTE("gte"),
    LT("lt"),
    LTE("lte"),
    BETWEEN("between");

    private final String token;

    FilterOperator(String token) {
        this.token = token;
    }

    public String token() {
        return token;
    }

    public static FilterOperator fromToken(String token) {
        return Arrays.stream(values())
            .filter(value -> value.token.equals(token))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported filter operator: " + token));
    }
}
