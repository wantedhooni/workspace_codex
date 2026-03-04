create table audit_logs (
    id uuid primary key,
    actor_type varchar(30) not null,
    actor_id uuid,
    actor_email varchar(120),
    action_type varchar(60) not null,
    target_type varchar(80) not null,
    target_id varchar(120),
    description varchar(255) not null,
    logged_at timestamp with time zone not null
);

create table customers (
    id uuid primary key,
    end_user_id uuid unique references end_users(id),
    customer_number varchar(40) not null unique,
    full_name varchar(120) not null,
    email varchar(120) not null unique,
    status varchar(30) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table accounts (
    id uuid primary key,
    customer_id uuid not null references customers(id),
    account_number varchar(40) not null unique,
    account_type varchar(30) not null,
    status varchar(30) not null,
    balance numeric(19, 4) not null,
    currency varchar(3) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table transactions (
    id uuid primary key,
    account_id uuid not null references accounts(id),
    transaction_number varchar(50) not null unique,
    transaction_type varchar(30) not null,
    status varchar(30) not null,
    amount numeric(19, 4) not null,
    currency varchar(3) not null,
    occurred_at timestamp with time zone not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);
