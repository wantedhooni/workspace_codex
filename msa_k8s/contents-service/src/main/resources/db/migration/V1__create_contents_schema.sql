create table contents.financial_content (
    id uuid primary key,
    type text not null,
    title text not null,
    body text not null,
    author_id uuid not null,
    status text not null,
    version bigint not null default 0,
    published_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint financial_content_type_valid check (type in ('NOTICE', 'POST')),
    constraint financial_content_title_length check (length(title) between 1 and 200),
    constraint financial_content_body_length check (length(body) between 1 and 100000),
    constraint financial_content_status_valid check (status in ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    constraint financial_content_publish_state check (
        (status = 'DRAFT' and published_at is null)
        or (status in ('PUBLISHED', 'ARCHIVED'))
    ),
    constraint financial_content_updated_after_created check (updated_at >= created_at)
);

create index financial_content_status_type_created_idx
    on contents.financial_content (status, type, created_at desc, id desc);

create index financial_content_author_created_idx
    on contents.financial_content (author_id, created_at desc, id desc);
