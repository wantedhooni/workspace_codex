package com.derivops.mvp.common.rsql;

import java.util.List;

public record RsqlExpression(
        String selector,
        String operator,
        List<String> arguments
) {
}
