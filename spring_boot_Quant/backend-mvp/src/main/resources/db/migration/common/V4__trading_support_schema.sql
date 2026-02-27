CREATE TABLE IF NOT EXISTS risk_limits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    max_order_notional DECIMAL(20,6) NOT NULL,
    max_position_notional_per_symbol DECIMAL(20,6) NOT NULL,
    max_daily_turnover DECIMAL(20,6) NOT NULL,
    max_open_orders_per_symbol INT NOT NULL,
    commission_bps DECIMAL(12,6) NOT NULL,
    slippage_bps DECIMAL(12,6) NOT NULL,
    trading_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    kill_switch_reason VARCHAR(255) NULL,
    kill_switch_updated_at TIMESTAMP(6) NULL,
    kill_switch_updated_by VARCHAR(120) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by VARCHAR(120) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(120) NOT NULL DEFAULT 'system',
    CONSTRAINT uk_risk_limits_portfolio UNIQUE (portfolio_id),
    CONSTRAINT fk_risk_limits_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id)
);

CREATE TABLE IF NOT EXISTS trading_control_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    previous_trading_enabled BOOLEAN NOT NULL,
    trading_enabled BOOLEAN NOT NULL,
    action VARCHAR(40) NOT NULL,
    reason VARCHAR(255),
    updated_at TIMESTAMP(6) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by VARCHAR(120) NOT NULL DEFAULT 'system',
    CONSTRAINT fk_tch_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id)
);

CREATE INDEX IF NOT EXISTS idx_tch_portfolio_updated_at ON trading_control_history(portfolio_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS order_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    portfolio_id BIGINT NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    action VARCHAR(40) NOT NULL,
    from_status VARCHAR(20),
    to_status VARCHAR(20),
    reason VARCHAR(255),
    actor VARCHAR(120) NOT NULL,
    acted_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by VARCHAR(120) NOT NULL DEFAULT 'system',
    CONSTRAINT fk_order_audit_logs_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_audit_logs_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id)
);

CREATE INDEX IF NOT EXISTS idx_order_audit_logs_order ON order_audit_logs(order_id, acted_at DESC);
CREATE INDEX IF NOT EXISTS idx_order_audit_logs_portfolio ON order_audit_logs(portfolio_id, acted_at DESC);

CREATE TABLE IF NOT EXISTS risk_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_key VARCHAR(120) NOT NULL,
    portfolio_id BIGINT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    code VARCHAR(80) NOT NULL,
    message VARCHAR(255) NOT NULL,
    metric_name VARCHAR(80),
    metric_value DECIMAL(20,6),
    threshold_value DECIMAL(20,6),
    workflow_status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    acknowledged_at TIMESTAMP(6),
    acknowledged_by VARCHAR(120),
    occurred_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by VARCHAR(120) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(120) NOT NULL DEFAULT 'system',
    CONSTRAINT uk_risk_alerts_alert_key UNIQUE (alert_key),
    CONSTRAINT fk_risk_alerts_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id)
);

CREATE INDEX IF NOT EXISTS idx_risk_alerts_portfolio_occurred ON risk_alerts(portfolio_id, occurred_at DESC);

CREATE TABLE IF NOT EXISTS saved_views (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_key VARCHAR(80) NOT NULL,
    view_name VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    shared BOOLEAN NOT NULL DEFAULT FALSE,
    owner_email VARCHAR(120) NOT NULL,
    filters_json TEXT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by VARCHAR(120) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(120) NOT NULL DEFAULT 'system'
);

CREATE INDEX IF NOT EXISTS idx_saved_views_owner_resource ON saved_views(owner_email, resource_key);

CREATE TABLE IF NOT EXISTS default_saved_views (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resource_key VARCHAR(80) NOT NULL,
    saved_view_id BIGINT NOT NULL,
    pinned_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by VARCHAR(120) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(120) NOT NULL DEFAULT 'system',
    CONSTRAINT uk_default_saved_views_user_resource UNIQUE (user_id, resource_key),
    CONSTRAINT fk_default_saved_views_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_default_saved_views_saved_view FOREIGN KEY (saved_view_id) REFERENCES saved_views(id)
);
