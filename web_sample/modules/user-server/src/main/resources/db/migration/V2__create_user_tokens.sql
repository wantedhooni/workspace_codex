create table user_refresh_tokens (
    id bigserial primary key,
    user_id bigint not null references users(id),
    token_hash varchar(64) not null unique,
    expires_at timestamptz not null,
    revoked_at timestamptz,
    created_at timestamptz not null
);

create index idx_user_refresh_tokens_user_id on user_refresh_tokens(user_id);
create index idx_user_refresh_tokens_expires_at on user_refresh_tokens(expires_at);

create table user_access_token_blacklist (
    id bigserial primary key,
    token_id varchar(100) not null unique,
    expires_at timestamptz not null,
    created_at timestamptz not null
);

create index idx_user_access_token_blacklist_expires_at on user_access_token_blacklist(expires_at);
