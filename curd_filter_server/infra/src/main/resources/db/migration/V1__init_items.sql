CREATE TABLE IF NOT EXISTS items (
    id VARCHAR(26) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    price NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_items_status ON items (status);
CREATE INDEX IF NOT EXISTS idx_items_created_at ON items (created_at);
