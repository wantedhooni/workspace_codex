create table admins (
    id bigserial primary key,
    email varchar(255) not null unique,
    password varchar(255) not null,
    name varchar(100) not null,
    role varchar(30) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);
