create sequence account.account_number_sequence
    as bigint
    start with 1
    increment by 1
    minvalue 1
    maxvalue 999999999
    no cycle
    cache 100;

create table account.bank_account (
    id uuid primary key,
    owner_id uuid not null,
    account_number text not null,
    currency text not null,
    balance numeric(19, 2) not null default 0,
    status text not null,
    version bigint not null default 0,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint bank_account_number_unique unique (account_number),
    constraint bank_account_number_format check (account_number ~ '^[0-9]{12}$'),
    constraint bank_account_currency_format check (currency ~ '^[A-Z]{3}$'),
    constraint bank_account_balance_non_negative check (balance >= 0),
    constraint bank_account_status_valid check (status in ('ACTIVE', 'SUSPENDED', 'CLOSED')),
    constraint bank_account_updated_after_created check (updated_at >= created_at)
);

create index bank_account_owner_created_idx
    on account.bank_account (owner_id, created_at, id);

create table account.account_ledger_entry (
    id uuid primary key,
    account_id uuid not null,
    idempotency_key text not null,
    type text not null,
    amount numeric(19, 2) not null,
    balance_after numeric(19, 2) not null,
    memo text,
    created_at timestamptz not null,
    constraint account_ledger_account_fk
        foreign key (account_id) references account.bank_account (id),
    constraint account_ledger_idempotency_unique unique (idempotency_key),
    constraint account_ledger_idempotency_length check (length(idempotency_key) between 1 and 100),
    constraint account_ledger_type_valid check (type in ('DEPOSIT', 'WITHDRAWAL')),
    constraint account_ledger_amount_positive check (amount > 0),
    constraint account_ledger_balance_non_negative check (balance_after >= 0),
    constraint account_ledger_memo_length check (memo is null or length(memo) <= 200)
);

create index account_ledger_account_created_idx
    on account.account_ledger_entry (account_id, created_at desc, id desc);
