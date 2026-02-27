package com.curd.template.domain.item;

import com.curd.template.core.filter.FilterExpression;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemRepositoryPort {
    Item save(Item item);

    Optional<Item> findById(String id);

    Page<Item> search(FilterExpression filterExpression, Pageable pageable);

    void deleteById(String id);
}
