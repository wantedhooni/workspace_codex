alter table exchange_requests add column if not exists cancellation_reason varchar(255);
alter table exchange_requests add column if not exists canceled_at timestamp with time zone;
