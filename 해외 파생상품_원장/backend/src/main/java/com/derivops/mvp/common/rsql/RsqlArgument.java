package com.derivops.mvp.common.rsql;

import java.util.List;

public sealed interface RsqlArgument permits RsqlArgument.SingleValue, RsqlArgument.MultiValue {

    record SingleValue(String value) implements RsqlArgument {
    }

    record MultiValue(List<String> values) implements RsqlArgument {
    }
}
