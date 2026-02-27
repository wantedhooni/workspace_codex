package com.curd.template.core.filter;

import com.curd.template.core.error.ApiErrorItem;
import com.curd.template.core.error.ApiException;
import com.curd.template.core.error.AppErrorCode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FilterPolicyValidator {

    public void validate(FilterExpression expression, FilterPolicy policy) {
        if (expression.totalClauseCount() > policy.maxClauses()) {
            throw new ApiException(
                AppErrorCode.FILTER_POLICY_VIOLATION,
                "Filter clause count exceeds max limit: " + policy.maxClauses()
            );
        }

        List<ApiErrorItem> errors = new ArrayList<>();
        validateClauses(expression.andClauses(), policy, errors);

        for (OrGroup orGroup : expression.orGroups()) {
            validateClauses(orGroup.clauses(), policy, errors);
        }

        if (!errors.isEmpty()) {
            throw new ApiException(
                AppErrorCode.FILTER_POLICY_VIOLATION,
                "Filter policy validation failed",
                errors
            );
        }
    }

    private void validateClauses(List<FilterClause> clauses, FilterPolicy policy, List<ApiErrorItem> errors) {
        for (FilterClause clause : clauses) {
            if (!policy.allows(clause.field(), clause.operator())) {
                errors.add(new ApiErrorItem(clause.field(), "Operator not allowed: " + clause.operator().token()));
            }

            if (clause.value() != null && clause.value().length() > policy.maxValueLength()) {
                errors.add(new ApiErrorItem(clause.field(), "Value exceeds max length: " + policy.maxValueLength()));
            }
        }
    }
}
