package com.curd.template.core.filter;

import com.curd.template.core.error.ApiException;
import com.curd.template.core.error.AppErrorCode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FilterQueryParser {

    public FilterExpression parse(List<String> filterParams) {
        List<FilterClause> andClauses = new ArrayList<>();

        if (filterParams != null) {
            for (String rawClause : filterParams) {
                if (rawClause == null || rawClause.isBlank()) {
                    continue;
                }
                andClauses.add(parseClause(rawClause));
            }
        }
        return new FilterExpression(andClauses, List.of());
    }

    private FilterClause parseClause(String rawClause) {
        String[] parts = rawClause.split(":", 3);
        if (parts.length < 2) {
            throw new ApiException(
                AppErrorCode.FILTER_PARSE_ERROR,
                "Invalid filter clause: " + rawClause
            );
        }

        String field = parts[0].trim();
        String operatorToken = parts[1].trim();
        String value = parts.length == 3 ? parts[2].trim() : null;

        if (field.isBlank()) {
            throw new ApiException(AppErrorCode.FILTER_PARSE_ERROR, "Filter field is empty");
        }

        try {
            FilterOperator operator = FilterOperator.fromToken(operatorToken);
            return new FilterClause(field, operator, value);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(AppErrorCode.FILTER_PARSE_ERROR, exception.getMessage());
        }
    }
}
