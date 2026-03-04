create table fx_rates (
    id uuid primary key,
    base_currency varchar(3) not null,
    quote_currency varchar(3) not null,
    rate numeric(19, 6) not null,
    effective_at timestamp with time zone not null,
    source varchar(80) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table exchange_requests (
    id uuid primary key,
    customer_id uuid not null references customers(id),
    account_id uuid not null references accounts(id),
    request_number varchar(40) not null unique,
    from_currency varchar(3) not null,
    to_currency varchar(3) not null,
    from_amount numeric(19, 4) not null,
    applied_rate numeric(19, 6) not null,
    to_amount numeric(19, 4) not null,
    status varchar(30) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table stock_orders (
    id uuid primary key,
    customer_id uuid not null references customers(id),
    account_id uuid not null references accounts(id),
    order_number varchar(40) not null unique,
    symbol varchar(20) not null,
    market varchar(20) not null,
    side varchar(10) not null,
    quantity numeric(19, 4) not null,
    limit_price numeric(19, 4) not null,
    gross_amount numeric(19, 4) not null,
    currency varchar(3) not null,
    status varchar(30) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);
