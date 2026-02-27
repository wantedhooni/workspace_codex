package com.curd.template.api.item;

import com.curd.template.app.item.ItemResult;

public final class ItemResponseMapper {

    private ItemResponseMapper() {
    }

    public static ItemResponse from(ItemResult result) {
        return new ItemResponse(
            result.id(),
            result.name(),
            result.description(),
            result.price(),
            result.status(),
            result.version(),
            result.createdAt(),
            result.updatedAt()
        );
    }
}
