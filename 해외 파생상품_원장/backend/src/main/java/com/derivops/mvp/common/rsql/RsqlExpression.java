package com.derivops.mvp.common.rsql;

public record RsqlExpression(
        String selector,
        RsqlOperator operator,
        RsqlArgument argument
) {
}
