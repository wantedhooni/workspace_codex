create table if not exists linked_bank_accounts (
    id uuid primary key,
    customer_id uuid not null,
    customer_email varchar(120) not null,
    bank_name varchar(80) not null,
    account_alias varchar(80) not null,
    account_holder_name varchar(120) not null,
    account_number varchar(40) not null,
    status varchar(30) not null,
    primary_withdrawal boolean not null default false,
    verified_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index if not exists idx_linked_bank_accounts_customer
    on linked_bank_accounts (customer_id, primary_withdrawal desc, created_at desc);

create unique index if not exists uq_linked_bank_accounts_customer_account
    on linked_bank_accounts (customer_id, account_number);

alter table funding_requests add column if not exists linked_bank_account_id uuid;
alter table funding_requests add column if not exists linked_bank_name varchar(80);
alter table funding_requests add column if not exists linked_bank_account_alias varchar(80);
alter table funding_requests add column if not exists linked_bank_account_number_masked varchar(20);
alter table funding_requests add column if not exists linked_bank_account_holder_name varchar(120);
