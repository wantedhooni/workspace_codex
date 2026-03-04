create table if not exists notifications (
    id uuid primary key,
    notification_key varchar(120) unique,
    recipient_type varchar(20) not null,
    recipient_id uuid not null,
    category varchar(30) not null,
    severity varchar(20) not null,
    title varchar(160) not null,
    message varchar(255) not null,
    action_path varchar(120) not null,
    reference_type varchar(40),
    reference_id uuid,
    read_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index if not exists idx_notifications_recipient on notifications (recipient_type, recipient_id, created_at desc);
create index if not exists idx_notifications_unread on notifications (recipient_type, recipient_id, read_at);
