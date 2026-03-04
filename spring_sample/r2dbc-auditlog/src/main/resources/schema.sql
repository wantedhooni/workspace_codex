CREATE TABLE IF NOT EXISTS approval_request (
    id BIGSERIAL PRIMARY KEY,
    request_number VARCHAR(60) NOT NULL UNIQUE,
    requester VARCHAR(80) NOT NULL,
    target_system VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(60) NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor VARCHAR(80) NOT NULL,
    detail VARCHAR(255) NOT NULL,
    logged_at TIMESTAMP NOT NULL
);
