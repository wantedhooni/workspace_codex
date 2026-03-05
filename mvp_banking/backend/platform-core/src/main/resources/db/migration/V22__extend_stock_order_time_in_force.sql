alter table stock_orders add column if not exists time_in_force varchar(20);
update stock_orders
set time_in_force = coalesce(time_in_force, 'DAY')
where time_in_force is null;
alter table stock_orders alter column time_in_force set not null;

alter table stock_orders add column if not exists expires_at timestamp with time zone;
update stock_orders
set expires_at = coalesce(expires_at, created_at + interval '1 day')
where expires_at is null;
alter table stock_orders alter column expires_at set not null;
