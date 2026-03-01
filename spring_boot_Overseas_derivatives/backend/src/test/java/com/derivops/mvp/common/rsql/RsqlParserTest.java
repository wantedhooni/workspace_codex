package com.derivops.mvp.common.rsql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.derivops.mvp.common.BadRequestException;
import java.util.List;
import org.junit.jupiter.api.Test;

class RsqlParserTest {

    @Test
    void shouldParseNestedOrAndGroups() {
        List<List<RsqlExpression>> groups = RsqlParser.parseToOrAndGroups(
                "status==ACTIVE;broker=in=(CME,EUREX),ownerName=like='macro desk'"
        );

        assertEquals(2, groups.size());
        assertEquals(new RsqlExpression("status", RsqlOperator.EQUAL, new RsqlArgument.SingleValue("ACTIVE")), groups.get(0).get(0));
        assertEquals(new RsqlExpression("broker", RsqlOperator.IN, new RsqlArgument.MultiValue(List.of("CME", "EUREX"))), groups.get(0).get(1));
        assertEquals(new RsqlExpression("ownerName", RsqlOperator.LIKE, new RsqlArgument.SingleValue("macro desk")), groups.get(1).get(0));
    }

    @Test
    void shouldRejectInOperatorWithoutParentheses() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> RsqlParser.parseToOrAndGroups("broker=in=CME")
        );

        assertEquals("RSQL IN/OUT requires parentheses: broker=in=CME", exception.getMessage());
    }
}
