package com.curd.template.core.sort;

import com.curd.template.core.error.ApiException;
import com.curd.template.core.error.AppErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class SortParser {

    public Sort parse(List<String> rawSortParams, Set<String> allowedFields) {
        if (rawSortParams == null || rawSortParams.isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (String rawSort : rawSortParams) {
            if (rawSort == null || rawSort.isBlank()) {
                continue;
            }
            for (String token : rawSort.split(",")) {
                String normalized = token.trim();
                if (normalized.isBlank()) {
                    continue;
                }

                boolean descending = normalized.startsWith("-");
                String field = descending ? normalized.substring(1) : normalized;
                if (!allowedFields.contains(field)) {
                    throw new ApiException(AppErrorCode.INVALID_SORT_FIELD, "Sort field not allowed: " + field);
                }

                orders.add(descending ? Sort.Order.desc(field) : Sort.Order.asc(field));
            }
        }

        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }
}
