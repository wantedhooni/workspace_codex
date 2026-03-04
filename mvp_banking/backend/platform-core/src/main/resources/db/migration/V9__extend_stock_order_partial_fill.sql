alter table stock_orders add column if not exists remaining_quantity numeric(19, 4);

update stock_orders
set remaining_quantity = greatest(quantity - coalesce(executed_quantity, 0), 0)
where remaining_quantity is null;

alter table stock_orders alter column remaining_quantity set not null;
