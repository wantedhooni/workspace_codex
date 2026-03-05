alter table stock_orders add column if not exists market_session varchar(30);
update stock_orders
set market_session = coalesce(market_session, 'REGULAR')
where market_session is null;
alter table stock_orders alter column market_session set not null;

alter table stock_orders add column if not exists expected_execution_at timestamp with time zone;
update stock_orders
set expected_execution_at = coalesce(expected_execution_at, created_at + interval '5 minute')
where expected_execution_at is null;
alter table stock_orders alter column expected_execution_at set not null;

alter table stock_orders add column if not exists manual_review_required boolean default false;
alter table stock_orders alter column manual_review_required set not null;

alter table stock_orders add column if not exists manual_review_reason varchar(255);

alter table stock_orders add column if not exists reference_price numeric(19, 4);
alter table stock_orders add column if not exists price_deviation_rate numeric(19, 6);
alter table stock_orders add column if not exists quote_effective_at timestamp with time zone;
alter table stock_orders add column if not exists quote_source varchar(80);
