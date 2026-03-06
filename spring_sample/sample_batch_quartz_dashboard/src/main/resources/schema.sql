CREATE TABLE IF NOT EXISTS task_import_request (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(80) NOT NULL,
    source_system VARCHAR(40) NOT NULL,
    account_no VARCHAR(40) NOT NULL,
    instrument_code VARCHAR(30) NOT NULL,
    market VARCHAR(10) NOT NULL,
    settlement_currency VARCHAR(3) NOT NULL,
    notional_amount NUMERIC(18, 2) NOT NULL,
    payload_size INTEGER NOT NULL,
    priority SMALLINT NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_task_import_request_processed_id
    ON task_import_request (processed, id);
CREATE INDEX IF NOT EXISTS idx_task_import_request_market_requested_at
    ON task_import_request (market, requested_at);

CREATE TABLE IF NOT EXISTS task_import_audit (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL UNIQUE,
    external_id VARCHAR(80) NOT NULL,
    source_system VARCHAR(40) NOT NULL,
    account_no VARCHAR(40) NOT NULL,
    instrument_code VARCHAR(30) NOT NULL,
    market VARCHAR(10) NOT NULL,
    settlement_currency VARCHAR(3) NOT NULL,
    notional_amount NUMERIC(18, 2) NOT NULL,
    priority SMALLINT NOT NULL,
    risk_bucket VARCHAR(20) NOT NULL,
    payload_size INTEGER NOT NULL,
    processing_latency_ms BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_task_import_audit_created_at
    ON task_import_audit (created_at DESC);

CREATE TABLE IF NOT EXISTS batch_sample_input (
    id BIGSERIAL PRIMARY KEY,
    account_no VARCHAR(40) NOT NULL,
    instrument_code VARCHAR(30) NOT NULL,
    quantity NUMERIC(18, 2) NOT NULL,
    unit_price NUMERIC(18, 2) NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_batch_sample_input_processed_id
    ON batch_sample_input (processed, id);

CREATE TABLE IF NOT EXISTS batch_sample_output (
    id BIGSERIAL PRIMARY KEY,
    input_id BIGINT NOT NULL UNIQUE,
    account_no VARCHAR(40) NOT NULL,
    instrument_code VARCHAR(30) NOT NULL,
    gross_amount NUMERIC(18, 2) NOT NULL,
    fee_amount NUMERIC(18, 2) NOT NULL,
    net_amount NUMERIC(18, 2) NOT NULL,
    risk_grade VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_batch_sample_output_created_at
    ON batch_sample_output (created_at DESC);
