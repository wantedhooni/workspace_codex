create table admin_users (
    id uuid primary key,
    email varchar(120) not null unique,
    password_hash varchar(255) not null,
    role varchar(40) not null,
    display_name varchar(80) not null,
    active boolean not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table end_users (
    id uuid primary key,
    email varchar(120) not null unique,
    password_hash varchar(255) not null,
    full_name varchar(120) not null,
    active boolean not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);
