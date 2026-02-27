package com.curd.template.infra.item;

import com.curd.template.domain.item.Item;

public final class ItemJpaMapper {

    private ItemJpaMapper() {
    }

    public static ItemJpaEntity toEntity(Item item) {
        ItemJpaEntity entity = new ItemJpaEntity();
        entity.setId(item.getId());
        entity.setName(item.getName());
        entity.setDescription(item.getDescription());
        entity.setPrice(item.getPrice());
        entity.setStatus(item.getStatus());
        entity.setVersion(item.getVersion());
        return entity;
    }

    public static Item toDomain(ItemJpaEntity entity) {
        return new Item(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getPrice(),
            entity.getStatus(),
            entity.getVersion(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
