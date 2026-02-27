package com.curd.template.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class FilterQueryParserTest {

    private final FilterQueryParser parser = new FilterQueryParser();

    @Test
    void parseAndClausesOnly() {
        FilterExpression expression = parser.parse(
            List.of("name:contains:phone", "price:gte:100", "status:eq:ACTIVE")
        );

        assertEquals(3, expression.andClauses().size());
        assertEquals(0, expression.orGroups().size());
    }
}
