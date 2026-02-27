package com.curd.template.app.filter;

import com.curd.template.core.filter.FilterOperation;
import com.curd.template.core.filter.FilterOperator;
import com.curd.template.core.filter.FilterPolicy;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ItemFilterPolicyConfiguration {

    public ItemFilterPolicyConfiguration(InMemoryFilterPolicyRegistry registry) {
        FilterPolicy readPolicy = new FilterPolicy(
            Map.of(
                "id", Set.of(FilterOperator.EQ, FilterOperator.NE, FilterOperator.IN),
                "name", Set.of(FilterOperator.EQ, FilterOperator.NE, FilterOperator.CONTAINS, FilterOperator.STARTS_WITH, FilterOperator.ENDS_WITH),
                "description", Set.of(FilterOperator.CONTAINS),
                "price", Set.of(FilterOperator.EQ, FilterOperator.NE, FilterOperator.GT, FilterOperator.GTE, FilterOperator.LT, FilterOperator.LTE, FilterOperator.BETWEEN, FilterOperator.IN),
                "status", Set.of(FilterOperator.EQ, FilterOperator.NE, FilterOperator.IN),
                "createdAt", Set.of(FilterOperator.GT, FilterOperator.GTE, FilterOperator.LT, FilterOperator.LTE, FilterOperator.BETWEEN),
                "updatedAt", Set.of(FilterOperator.GT, FilterOperator.GTE, FilterOperator.LT, FilterOperator.LTE, FilterOperator.BETWEEN)
            ),
            Set.of("id", "name", "price", "status", "createdAt", "updatedAt"),
            20,
            200,
            100
        );

        FilterPolicy nonReadPolicy = new FilterPolicy(Map.of(), Set.of(), 1, 1, 100);

        registry.register("items", FilterOperation.READ, readPolicy);
        registry.register("items", FilterOperation.CREATE, nonReadPolicy);
        registry.register("items", FilterOperation.UPDATE, nonReadPolicy);
        registry.register("items", FilterOperation.PATCH, nonReadPolicy);
        registry.register("items", FilterOperation.DELETE, nonReadPolicy);
    }
}
