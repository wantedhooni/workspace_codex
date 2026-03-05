alter table linked_bank_accounts add column if not exists block_reason_code varchar(50);
alter table linked_bank_accounts add column if not exists blocked_at timestamp with time zone;
