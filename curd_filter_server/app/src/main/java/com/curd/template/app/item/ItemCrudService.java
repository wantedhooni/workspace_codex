package com.curd.template.app.item;

import com.curd.template.app.common.AuthorizationHook;
import com.curd.template.app.common.CrudApplicationService;
import com.curd.template.core.error.ApiException;
import com.curd.template.core.error.AppErrorCode;
import com.curd.template.core.id.IdGenerator;
import com.curd.template.domain.item.Item;
import com.curd.template.domain.item.ItemRepositoryPort;
import com.curd.template.domain.item.ItemStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ItemCrudService extends CrudApplicationService<
    Item,
    String,
    CreateItemCommand,
    UpdateItemCommand,
    PatchItemCommand,
    ItemSearchQuery,
    ItemResult
> {

    private final ItemRepositoryPort itemRepository;
    private final IdGenerator idGenerator;

    public ItemCrudService(
        ItemRepositoryPort itemRepository,
        Optional<AuthorizationHook> authorizationHook,
        IdGenerator idGenerator
    ) {
        super(authorizationHook.orElseGet(AuthorizationHook::noop));
        this.itemRepository = itemRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    protected void validateCreate(CreateItemCommand command) {
        ensureName(command.name());
        ensurePrice(command.price());
    }

    @Override
    protected void validateUpdate(String id, UpdateItemCommand command) {
        ensureId(id);
        ensureName(command.name());
        ensurePrice(command.price());
    }

    @Override
    protected void validatePatch(String id, PatchItemCommand command) {
        ensureId(id);
        if (command.name() != null && command.name().isBlank()) {
            throw new ApiException(AppErrorCode.VALIDATION_ERROR, "name must not be blank");
        }
        if (command.price() != null && command.price().signum() < 0) {
            throw new ApiException(AppErrorCode.VALIDATION_ERROR, "price must be >= 0");
        }
    }

    @Override
    @Transactional
    protected Item doCreate(CreateItemCommand command) {
        ItemStatus status = command.status() == null ? ItemStatus.ACTIVE : command.status();
        Item item = Item.create(idGenerator.nextId(), command.name(), command.description(), command.price(), status);
        return itemRepository.save(item);
    }

    @Override
    protected Optional<Item> doGet(String id) {
        return itemRepository.findById(id);
    }

    @Override
    protected Page<Item> doSearch(ItemSearchQuery query, Pageable pageable) {
        return itemRepository.search(query.filterExpression(), pageable);
    }

    @Override
    @Transactional
    protected Item doUpdate(String id, UpdateItemCommand command) {
        Item current = itemRepository.findById(id)
            .orElseThrow(() -> new ApiException(AppErrorCode.RESOURCE_NOT_FOUND, "Item not found: " + id));
        return itemRepository.save(current.replace(command.name(), command.description(), command.price(), command.status()));
    }

    @Override
    @Transactional
    protected Item doPatch(String id, PatchItemCommand command) {
        Item current = itemRepository.findById(id)
            .orElseThrow(() -> new ApiException(AppErrorCode.RESOURCE_NOT_FOUND, "Item not found: " + id));
        return itemRepository.save(current.patch(command.name(), command.description(), command.price(), command.status()));
    }

    @Override
    @Transactional
    protected void doDelete(String id) {
        if (itemRepository.findById(id).isEmpty()) {
            throw new ApiException(AppErrorCode.RESOURCE_NOT_FOUND, "Item not found: " + id);
        }
        itemRepository.deleteById(id);
    }

    @Override
    protected ItemResult mapToResult(Item model) {
        return new ItemResult(
            model.getId(),
            model.getName(),
            model.getDescription(),
            model.getPrice(),
            model.getStatus(),
            model.getVersion(),
            model.getCreatedAt(),
            model.getUpdatedAt()
        );
    }

    private void ensureName(String name) {
        if (name == null || name.isBlank()) {
            throw new ApiException(AppErrorCode.VALIDATION_ERROR, "name must not be blank");
        }
    }

    private void ensurePrice(java.math.BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new ApiException(AppErrorCode.VALIDATION_ERROR, "price must be >= 0");
        }
    }

    private void ensureId(String id) {
        if (id == null || id.isBlank()) {
            throw new ApiException(AppErrorCode.VALIDATION_ERROR, "id must not be blank");
        }
    }
}
