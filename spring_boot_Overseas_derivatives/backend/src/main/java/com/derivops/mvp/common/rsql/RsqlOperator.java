package com.derivops.mvp.common.rsql;

import java.util.Arrays;
import java.util.Comparator;

public enum RsqlOperator {
    NOT_IN("=out=", true),
    IN("=in=", true),
    LIKE("=like=", false),
    GREATER_THAN_OR_EQUAL("=ge=", false),
    LESS_THAN_OR_EQUAL("=le=", false),
    GREATER_THAN("=gt=", false),
    LESS_THAN("=lt=", false),
    EQUAL("==", false),
    NOT_EQUAL("!=", false);

    private static final RsqlOperator[] VALUES_BY_TOKEN_LENGTH = Arrays.stream(values())
            .sorted(Comparator.comparingInt((RsqlOperator operator) -> operator.token.length()).reversed())
            .toArray(RsqlOperator[]::new);

    private final String token;
    private final boolean multiValue;

    RsqlOperator(String token, boolean multiValue) {
        this.token = token;
        this.multiValue = multiValue;
    }

    public String token() {
        return token;
    }

    public boolean isMultiValue() {
        return multiValue;
    }

    public static RsqlOperator fromExpression(String expression) {
        return Arrays.stream(VALUES_BY_TOKEN_LENGTH)
                .filter(operator -> expression.contains(operator.token))
                .findFirst()
                .orElse(null);
    }
}
