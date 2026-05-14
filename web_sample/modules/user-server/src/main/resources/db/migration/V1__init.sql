create table users (
    id bigserial primary key,
    email varchar(255) not null unique,
    password varchar(255) not null,
    name varchar(100) not null,
    role varchar(30) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table posts (
    id bigserial primary key,
    title varchar(200) not null,
    content text not null,
    author_id bigint not null references users(id),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_posts_author_id on posts(author_id);
create index idx_posts_created_at on posts(created_at desc);
