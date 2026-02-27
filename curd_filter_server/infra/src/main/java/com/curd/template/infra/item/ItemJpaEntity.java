package com.curd.template.infra.item;

import com.curd.template.core.audit.BaseAuditableEntity;
import com.curd.template.domain.item.ItemStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;

@Entity
@Table(
    name = "items",
    indexes = {
        @Index(name = "idx_items_status", columnList = "status"),
        @Index(name = "idx_items_created_at", columnList = "created_at")
    }
)
public class ItemJpaEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 26)
    private String id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ItemStatus status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
