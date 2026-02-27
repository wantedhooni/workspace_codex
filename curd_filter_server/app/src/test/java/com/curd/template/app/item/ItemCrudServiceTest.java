package com.curd.template.app.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.curd.template.core.id.IdGenerator;
import com.curd.template.domain.item.Item;
import com.curd.template.domain.item.ItemRepositoryPort;
import com.curd.template.domain.item.ItemStatus;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ItemCrudServiceTest {

    @Test
    void createItem() {
        ItemRepositoryPort repository = Mockito.mock(ItemRepositoryPort.class);
        IdGenerator idGenerator = () -> "01TESTID000000000000000000";

        Item saved = Item.create("01TESTID000000000000000000", "item", "desc", new BigDecimal("10.00"), ItemStatus.ACTIVE);
        when(repository.save(any(Item.class))).thenReturn(saved);

        ItemCrudService service = new ItemCrudService(repository, Optional.empty(), idGenerator);
        ItemResult result = service.create(new CreateItemCommand("item", "desc", new BigDecimal("10.00"), ItemStatus.ACTIVE));

        assertEquals("01TESTID000000000000000000", result.id());
        verify(repository).save(any(Item.class));
    }
}
