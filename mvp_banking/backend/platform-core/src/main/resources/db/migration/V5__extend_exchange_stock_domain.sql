alter table transactions add column if not exists description varchar(255);

alter table exchange_requests add column if not exists settlement_transaction_number varchar(50);
alter table exchange_requests add column if not exists settled_at timestamp with time zone;

alter table stock_orders add column if not exists executed_quantity numeric(19, 4);
alter table stock_orders add column if not exists executed_price numeric(19, 4);
alter table stock_orders add column if not exists settlement_transaction_number varchar(50);
alter table stock_orders add column if not exists settled_at timestamp with time zone;

create table if not exists stock_positions (
    id uuid primary key,
    customer_id uuid not null references customers(id),
    account_id uuid not null references accounts(id),
    symbol varchar(20) not null,
    market varchar(20) not null,
    quantity numeric(19, 4) not null,
    average_price numeric(19, 4) not null,
    currency varchar(3) not null,
    realized_profit_loss numeric(19, 4) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);
