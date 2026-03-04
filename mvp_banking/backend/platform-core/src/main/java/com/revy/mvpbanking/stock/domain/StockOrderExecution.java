package com.revy.mvpbanking.stock.domain;

import com.revy.mvpbanking.common.domain.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_order_executions")
public class StockOrderExecution extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "execution_number", nullable = false, unique = true, length = 50)
    private String executionNumber;

    @Column(name = "execution_sequence", nullable = false)
    private Integer executionSequence;

    @Column(name = "executed_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal executedQuantity;

    @Column(name = "executed_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal executedPrice;

    @Column(name = "executed_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal executedAmount;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    protected StockOrderExecution() {
    }

    public StockOrderExecution(
            UUID orderId,
            String executionNumber,
            Integer executionSequence,
            BigDecimal executedQuantity,
            BigDecimal executedPrice,
            BigDecimal executedAmount,
            Instant executedAt
    ) {
        this.orderId = orderId;
        this.executionNumber = executionNumber;
        this.executionSequence = executionSequence;
        this.executedQuantity = executedQuantity;
        this.executedPrice = executedPrice;
        this.executedAmount = executedAmount;
        this.executedAt = executedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getExecutionNumber() {
        return executionNumber;
    }

    public Integer getExecutionSequence() {
        return executionSequence;
    }

    public BigDecimal getExecutedQuantity() {
        return executedQuantity;
    }

    public BigDecimal getExecutedPrice() {
        return executedPrice;
    }

    public BigDecimal getExecutedAmount() {
        return executedAmount;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }
}
