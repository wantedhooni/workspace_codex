create table if not exists funding_requests (
    id uuid primary key,
    customer_id uuid not null,
    customer_email varchar(120) not null,
    account_id uuid not null,
    account_number varchar(40) not null,
    account_type varchar(30) not null,
    request_number varchar(50) unique not null,
    request_type varchar(20) not null,
    status varchar(20) not null,
    amount numeric(19, 4) not null,
    currency varchar(3) not null,
    balance_snapshot numeric(19, 4) not null,
    note varchar(255),
    settlement_transaction_number varchar(50),
    settled_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index if not exists idx_funding_requests_customer_created
    on funding_requests (customer_id, created_at desc);

create index if not exists idx_funding_requests_status_type
    on funding_requests (status, request_type, created_at desc);
