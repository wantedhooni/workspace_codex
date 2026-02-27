package com.curd.template.core.filter;

import com.curd.template.core.error.ApiException;
import com.curd.template.core.error.AppErrorCode;
import java.util.Map;
import java.util.Objects;

public final class FilterUsageGuard {

    private FilterUsageGuard() {
    }

    public static void assertNoFilterParameters(Map<String, ?> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return;
        }

        if (queryParams.containsKey("and") || queryParams.containsKey("or") || queryParams.containsKey("sort")) {
            throw new ApiException(
                AppErrorCode.FILTER_NOT_ALLOWED,
                "Filter and sort parameters are only allowed for READ collection operations"
            );
        }

        boolean hasReservedParameter = queryParams.keySet().stream().filter(Objects::nonNull)
            .anyMatch(key -> key.startsWith("and") || key.startsWith("or") || key.startsWith("sort"));
        if (hasReservedParameter) {
            throw new ApiException(
                AppErrorCode.FILTER_NOT_ALLOWED,
                "Reserved filter parameter usage is not allowed for this operation"
            );
        }
    }
}
