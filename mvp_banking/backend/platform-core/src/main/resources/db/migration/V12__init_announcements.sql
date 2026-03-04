create table if not exists announcements (
    id uuid primary key,
    announcement_key varchar(120) unique not null,
    title varchar(160) not null,
    summary varchar(255) not null,
    body text not null,
    severity varchar(20) not null,
    audience varchar(20) not null,
    status varchar(20) not null,
    pinned boolean not null default false,
    starts_at timestamp with time zone,
    ends_at timestamp with time zone,
    published_at timestamp with time zone,
    archived_at timestamp with time zone,
    created_by_email varchar(120) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index if not exists idx_announcements_status_audience on announcements (status, audience, pinned desc, created_at desc);
