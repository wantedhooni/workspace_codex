package com.tradeauto.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "quartz_audit", indexes = {
        @Index(name = "idx_quartz_audit_created", columnList = "created_at")
})
public class QuartzAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String action;

    @Column(nullable = false, length = 200)
    private String target;

    @Column(length = 1000)
    private String detail;

    @Column(nullable = false, length = 80)
    private String actor;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Long getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
