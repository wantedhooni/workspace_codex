package com.curd.template.core.filter;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.curd.template.core.error.ApiException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FilterPolicyValidatorTest {

    private final FilterPolicyValidator validator = new FilterPolicyValidator();

    @Test
    void throwWhenOperatorNotAllowed() {
        FilterPolicy policy = new FilterPolicy(
            Map.of("name", Set.of(FilterOperator.EQ)),
            Set.of("name"),
            10,
            100,
            50
        );

        FilterExpression expression = new FilterExpression(
            List.of(new FilterClause("name", FilterOperator.CONTAINS, "abc")),
            List.of()
        );

        assertThrows(ApiException.class, () -> validator.validate(expression, policy));
    }
}
