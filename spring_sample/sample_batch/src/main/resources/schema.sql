CREATE TABLE IF NOT EXISTS trade_raw_event (
    id BIGSERIAL PRIMARY KEY,
    account_no VARCHAR(40) NOT NULL,
    instrument_code VARCHAR(30) NOT NULL,
    quantity NUMERIC(18, 2) NOT NULL,
    price NUMERIC(18, 2) NOT NULL,
    market VARCHAR(10) NOT NULL,
    executed_at TIMESTAMP NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_trade_raw_event_processed_id
    ON trade_raw_event (processed, id);

CREATE TABLE IF NOT EXISTS trade_settlement_summary (
    id BIGSERIAL PRIMARY KEY,
    source_event_id BIGINT NOT NULL UNIQUE,
    account_no VARCHAR(40) NOT NULL,
    instrument_code VARCHAR(30) NOT NULL,
    gross_amount NUMERIC(18, 2) NOT NULL,
    fee_amount NUMERIC(18, 2) NOT NULL,
    net_amount NUMERIC(18, 2) NOT NULL,
    settlement_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_trade_settlement_summary_settlement_date
    ON trade_settlement_summary (settlement_date);
