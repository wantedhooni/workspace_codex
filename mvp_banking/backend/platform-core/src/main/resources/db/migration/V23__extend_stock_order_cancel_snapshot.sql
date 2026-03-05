alter table stock_orders add column if not exists cancellation_reason varchar(255);
alter table stock_orders add column if not exists canceled_at timestamp with time zone;
