create table admin_refresh_tokens (
    id bigserial primary key,
    admin_id bigint not null references admins(id),
    token_hash varchar(64) not null unique,
    expires_at timestamptz not null,
    revoked_at timestamptz,
    created_at timestamptz not null
);

create index idx_admin_refresh_tokens_admin_id on admin_refresh_tokens(admin_id);
create index idx_admin_refresh_tokens_expires_at on admin_refresh_tokens(expires_at);

create table admin_access_token_blacklist (
    id bigserial primary key,
    token_id varchar(100) not null unique,
    expires_at timestamptz not null,
    created_at timestamptz not null
);

create index idx_admin_access_token_blacklist_expires_at on admin_access_token_blacklist(expires_at);
