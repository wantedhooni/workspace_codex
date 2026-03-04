CREATE TABLE IF NOT EXISTS task_import_request (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(80) NOT NULL,
    payload_size INTEGER NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_task_import_request_processed_id
    ON task_import_request (processed, id);

CREATE TABLE IF NOT EXISTS task_import_audit (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL UNIQUE,
    external_id VARCHAR(80) NOT NULL,
    payload_size INTEGER NOT NULL,
    processing_latency_ms BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
