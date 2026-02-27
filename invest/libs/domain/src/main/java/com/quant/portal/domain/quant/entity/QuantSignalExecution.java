package com.quant.portal.domain.quant.entity;

import com.quant.portal.common.jpa.BaseAuditUserEntity;
import com.quant.portal.domain.portfolio.entity.PortfolioTransaction;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;

@Entity
@Table(
        name = "quant_signal_executions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_quant_signal_execution_signal", columnNames = "signal_id")
        }
)
public class QuantSignalExecution extends BaseAuditUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signal_id", nullable = false)
    private QuantSignal signal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private PortfolioTransaction transaction;

    protected QuantSignalExecution() {
    }

    public QuantSignalExecution(QuantSignal signal, PortfolioTransaction transaction) {
        this.signal = Objects.requireNonNull(signal, "signal must not be null");
        this.transaction = Objects.requireNonNull(transaction, "transaction must not be null");
    }

    public Long getId() {
        return id;
    }

    public QuantSignal getSignal() {
        return signal;
    }

    public PortfolioTransaction getTransaction() {
        return transaction;
    }
}
