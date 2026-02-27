CREATE TABLE job_runs (
  id BIGSERIAL PRIMARY KEY,
  job_name VARCHAR(80) NOT NULL,
  status VARCHAR(20) NOT NULL,
  started_at TIMESTAMPTZ NOT NULL,
  ended_at TIMESTAMPTZ,
  message VARCHAR(1000)
);

CREATE INDEX idx_job_runs_name_start ON job_runs(job_name, started_at);
