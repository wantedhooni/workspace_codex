CREATE TABLE quartz_audit (
  id BIGSERIAL PRIMARY KEY,
  action VARCHAR(40) NOT NULL,
  target VARCHAR(200) NOT NULL,
  detail VARCHAR(1000),
  actor VARCHAR(80) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_quartz_audit_created ON quartz_audit(created_at);
