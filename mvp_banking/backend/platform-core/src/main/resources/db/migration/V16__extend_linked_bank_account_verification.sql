alter table linked_bank_accounts add column if not exists verification_reference varchar(20);
alter table linked_bank_accounts add column if not exists verification_requested_at timestamp with time zone;
alter table linked_bank_accounts add column if not exists verification_attempt_count integer not null default 0;
