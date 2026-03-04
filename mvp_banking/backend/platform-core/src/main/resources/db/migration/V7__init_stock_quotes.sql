create table if not exists stock_quotes (
    id uuid primary key,
    symbol varchar(20) not null,
    market varchar(20) not null,
    price numeric(19, 4) not null,
    currency varchar(3) not null,
    change_rate numeric(19, 6) not null,
    effective_at timestamp with time zone not null,
    source varchar(80) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);
