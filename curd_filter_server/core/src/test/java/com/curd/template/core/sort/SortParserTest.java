package com.curd.template.core.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.curd.template.core.error.ApiException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class SortParserTest {

    private final SortParser sortParser = new SortParser();

    @Test
    void parseSortOrders() {
        Sort sort = sortParser.parse(List.of("name,-createdAt"), Set.of("name", "createdAt"));
        assertEquals(2, sort.stream().count());
    }

    @Test
    void rejectNotAllowedSortField() {
        assertThrows(ApiException.class, () -> sortParser.parse(List.of("secretField"), Set.of("name")));
    }
}
