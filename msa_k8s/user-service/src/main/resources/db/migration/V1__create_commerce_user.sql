create table commerce_user (
    id uuid primary key,
    email text not null,
    name text not null,
    status text not null,
    created_at timestamptz not null,
    constraint uk_commerce_user_email unique (email),
    constraint ck_commerce_user_email_length check (char_length(email) between 3 and 254),
    constraint ck_commerce_user_name_length check (char_length(name) between 1 and 100),
    constraint ck_commerce_user_status check (status in ('ACTIVE', 'SUSPENDED'))
);

create index idx_commerce_user_created_at_id
    on commerce_user (created_at, id);

