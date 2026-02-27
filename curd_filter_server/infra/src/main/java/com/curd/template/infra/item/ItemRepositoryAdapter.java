package com.curd.template.infra.item;

import com.curd.template.core.filter.FilterExpression;
import com.curd.template.domain.item.Item;
import com.curd.template.domain.item.ItemRepositoryPort;
import com.curd.template.infra.filter.QuerydslPredicateFactory;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class ItemRepositoryAdapter implements ItemRepositoryPort {

    private final SpringDataItemRepository repository;
    private final QuerydslPredicateFactory predicateFactory;

    public ItemRepositoryAdapter(
        SpringDataItemRepository repository,
        QuerydslPredicateFactory predicateFactory
    ) {
        this.repository = repository;
        this.predicateFactory = predicateFactory;
    }

    @Override
    public Item save(Item item) {
        ItemJpaEntity saved = repository.save(ItemJpaMapper.toEntity(item));
        return ItemJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Item> findById(String id) {
        return repository.findById(id).map(ItemJpaMapper::toDomain);
    }

    @Override
    public Page<Item> search(FilterExpression filterExpression, Pageable pageable) {
        return repository.findAll(predicateFactory.toPredicate(filterExpression), pageable)
            .map(ItemJpaMapper::toDomain);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
