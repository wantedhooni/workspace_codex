create table approval_requests (
    id uuid primary key,
    target_type varchar(40) not null,
    target_id uuid not null,
    title varchar(160) not null,
    description varchar(255) not null,
    status varchar(20) not null,
    requested_by_email varchar(120) not null,
    decision_by_email varchar(120),
    decision_reason varchar(255),
    decided_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);
