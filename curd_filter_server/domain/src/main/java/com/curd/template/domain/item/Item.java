package com.curd.template.domain.item;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public final class Item {
    private final String id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final ItemStatus status;
    private final Long version;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Item(
        String id,
        String name,
        String description,
        BigDecimal price,
        ItemStatus status,
        Long version,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = requireText(id, "id");
        this.name = requireText(name, "name");
        this.description = description;
        this.price = requirePositive(price, "price");
        this.status = Objects.requireNonNull(status, "status");
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Item create(String id, String name, String description, BigDecimal price, ItemStatus status) {
        return new Item(id, name, description, price, status, null, null, null);
    }

    public Item replace(String name, String description, BigDecimal price, ItemStatus status) {
        return new Item(
            this.id,
            requireText(name, "name"),
            description,
            requirePositive(price, "price"),
            Objects.requireNonNull(status, "status"),
            this.version,
            this.createdAt,
            this.updatedAt
        );
    }

    public Item patch(String name, String description, BigDecimal price, ItemStatus status) {
        return new Item(
            this.id,
            name == null || name.isBlank() ? this.name : name,
            description == null ? this.description : description,
            price == null ? this.price : requirePositive(price, "price"),
            status == null ? this.status : status,
            this.version,
            this.createdAt,
            this.updatedAt
        );
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static BigDecimal requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " must be >= 0");
        }
        return value;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
