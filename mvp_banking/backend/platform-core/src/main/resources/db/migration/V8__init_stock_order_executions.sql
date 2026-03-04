create table if not exists stock_order_executions (
    id uuid primary key,
    order_id uuid not null references stock_orders(id),
    execution_number varchar(50) not null unique,
    execution_sequence integer not null,
    executed_quantity numeric(19, 4) not null,
    executed_price numeric(19, 4) not null,
    executed_amount numeric(19, 4) not null,
    executed_at timestamp with time zone not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);
