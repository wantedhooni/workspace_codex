package com.example.samplerecommend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 고객의 최근 행동 데이터를 적재해 추천 가중치 계산에 사용한다.
 */
@Entity
@Table(name = "customer_actions")
public class CustomerAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String customerId;

    @Column(nullable = false, length = 40)
    private String productCode;

    @Column(nullable = false, length = 20)
    private String actionType;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private LocalDateTime actedAt;

    protected CustomerAction() {
    }

    public CustomerAction(String customerId, String productCode, String actionType, Double weight, LocalDateTime actedAt) {
        this.customerId = customerId;
        this.productCode = productCode;
        this.actionType = actionType;
        this.weight = weight;
        this.actedAt = actedAt;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getActionType() {
        return actionType;
    }

    public Double getWeight() {
        return weight;
    }

    public LocalDateTime getActedAt() {
        return actedAt;
    }
}

