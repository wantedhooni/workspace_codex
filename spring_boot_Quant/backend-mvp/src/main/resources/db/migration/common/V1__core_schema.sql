CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(120) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE TABLE IF NOT EXISTS portfolios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    side VARCHAR(10) NOT NULL,
    order_type VARCHAR(20) NOT NULL,
    quantity DECIMAL(20,6) NOT NULL,
    limit_price DECIMAL(20,6),
    status VARCHAR(20) NOT NULL,
    broker_order_id VARCHAR(80),
    trade_date DATE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_orders_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id)
);

CREATE INDEX IF NOT EXISTS idx_orders_portfolio_date ON orders(portfolio_id, trade_date);
CREATE INDEX IF NOT EXISTS idx_orders_symbol_date ON orders(symbol, trade_date);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

CREATE TABLE IF NOT EXISTS trades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    broker_trade_id VARCHAR(80) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    side VARCHAR(10) NOT NULL,
    trade_price DECIMAL(20,6) NOT NULL,
    trade_qty DECIMAL(20,6) NOT NULL,
    fee_amount DECIMAL(20,6) NOT NULL DEFAULT 0,
    trade_status VARCHAR(20) NOT NULL,
    traded_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_trades_broker_trade_id UNIQUE (broker_trade_id),
    CONSTRAINT fk_trades_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX IF NOT EXISTS idx_trades_order ON trades(order_id);
CREATE INDEX IF NOT EXISTS idx_trades_symbol_time ON trades(symbol, traded_at);

CREATE TABLE IF NOT EXISTS positions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    quantity DECIMAL(20,6) NOT NULL,
    avg_price DECIMAL(20,6) NOT NULL,
    realized_pnl DECIMAL(20,6) NOT NULL DEFAULT 0,
    unrealized_pnl DECIMAL(20,6) NOT NULL DEFAULT 0,
    as_of_date DATE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_positions_portfolio_symbol_date UNIQUE (portfolio_id, symbol, as_of_date),
    CONSTRAINT fk_positions_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id)
);

CREATE INDEX IF NOT EXISTS idx_positions_portfolio_date ON positions(portfolio_id, as_of_date);

CREATE TABLE IF NOT EXISTS journal_vouchers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    voucher_no VARCHAR(40) NOT NULL UNIQUE,
    portfolio_id BIGINT NOT NULL,
    voucher_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    approved_by BIGINT,
    approved_at TIMESTAMP(6),
    posted_at TIMESTAMP(6),
    description VARCHAR(300),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_jv_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id),
    CONSTRAINT fk_jv_approved_by FOREIGN KEY (approved_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS journal_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    voucher_id BIGINT NOT NULL,
    line_no INT NOT NULL,
    account_code VARCHAR(30) NOT NULL,
    dr_cr CHAR(2) NOT NULL,
    amount DECIMAL(20,6) NOT NULL,
    symbol VARCHAR(20),
    trade_id BIGINT,
    description VARCHAR(300),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_journal_entries_voucher_line UNIQUE (voucher_id, line_no),
    CONSTRAINT fk_je_voucher FOREIGN KEY (voucher_id) REFERENCES journal_vouchers(id),
    CONSTRAINT fk_je_trade FOREIGN KEY (trade_id) REFERENCES trades(id)
);

CREATE TABLE IF NOT EXISTS ledger_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_code VARCHAR(30) NOT NULL,
    portfolio_id BIGINT NOT NULL,
    entry_date DATE NOT NULL,
    dr_cr CHAR(2) NOT NULL,
    amount DECIMAL(20,6) NOT NULL,
    voucher_id BIGINT,
    entry_ref VARCHAR(80),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_le_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id),
    CONSTRAINT fk_le_voucher FOREIGN KEY (voucher_id) REFERENCES journal_vouchers(id)
);

CREATE INDEX IF NOT EXISTS idx_ledger_account_date ON ledger_entries(account_code, entry_date);
CREATE INDEX IF NOT EXISTS idx_ledger_portfolio_date ON ledger_entries(portfolio_id, entry_date);
