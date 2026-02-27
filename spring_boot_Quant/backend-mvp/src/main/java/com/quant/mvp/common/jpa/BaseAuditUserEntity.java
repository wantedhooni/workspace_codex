package com.quant.mvp.common.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

@MappedSuperclass
public abstract class BaseAuditUserEntity extends BaseAuditEntity {

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 120)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 120)
    private String updatedBy;

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }
}
