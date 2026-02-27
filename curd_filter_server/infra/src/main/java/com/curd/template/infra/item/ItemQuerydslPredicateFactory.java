package com.curd.template.infra.item;

import com.curd.template.core.filter.FilterExpression;
import com.curd.template.domain.item.ItemStatus;
import com.curd.template.infra.filter.BetweenStrategy;
import com.curd.template.infra.filter.ContainsStrategy;
import com.curd.template.infra.filter.DefaultQuerydslPredicateFactory;
import com.curd.template.infra.filter.EndsWithStrategy;
import com.curd.template.infra.filter.EqStrategy;
import com.curd.template.infra.filter.FilterOperatorStrategy;
import com.curd.template.infra.filter.GtStrategy;
import com.curd.template.infra.filter.GteStrategy;
import com.curd.template.infra.filter.InStrategy;
import com.curd.template.infra.filter.IsNotNullStrategy;
import com.curd.template.infra.filter.IsNullStrategy;
import com.curd.template.infra.filter.LtStrategy;
import com.curd.template.infra.filter.LteStrategy;
import com.curd.template.infra.filter.NeStrategy;
import com.curd.template.infra.filter.QuerydslPredicateFactory;
import com.curd.template.infra.filter.StartsWithStrategy;
import com.querydsl.core.types.Predicate;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ItemQuerydslPredicateFactory implements QuerydslPredicateFactory {

    private final DefaultQuerydslPredicateFactory delegate;

    public ItemQuerydslPredicateFactory() {
        List<FilterOperatorStrategy> strategies = List.of(
            new EqStrategy(),
            new NeStrategy(),
            new IsNullStrategy(),
            new IsNotNullStrategy(),
            new InStrategy(),
            new ContainsStrategy(),
            new StartsWithStrategy(),
            new EndsWithStrategy(),
            new GtStrategy(),
            new GteStrategy(),
            new LtStrategy(),
            new LteStrategy(),
            new BetweenStrategy()
        );

        Map<String, Class<?>> fieldTypes = Map.of(
            "id", String.class,
            "name", String.class,
            "description", String.class,
            "price", BigDecimal.class,
            "status", ItemStatus.class,
            "createdAt", Instant.class,
            "updatedAt", Instant.class
        );

        this.delegate = new DefaultQuerydslPredicateFactory(ItemJpaEntity.class, "itemJpaEntity", fieldTypes, strategies);
    }

    @Override
    public Predicate toPredicate(FilterExpression expression) {
        return delegate.toPredicate(expression);
    }
}
