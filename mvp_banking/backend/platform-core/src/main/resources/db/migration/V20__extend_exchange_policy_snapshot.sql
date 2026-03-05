alter table exchange_requests add column if not exists applied_rate_effective_at timestamp with time zone;
update exchange_requests
set applied_rate_effective_at = coalesce(applied_rate_effective_at, created_at)
where applied_rate_effective_at is null;
alter table exchange_requests alter column applied_rate_effective_at set not null;

alter table exchange_requests add column if not exists same_day_settlement_eligible boolean default true;
alter table exchange_requests alter column same_day_settlement_eligible set not null;

alter table exchange_requests add column if not exists expected_settlement_at timestamp with time zone;
update exchange_requests
set expected_settlement_at = coalesce(expected_settlement_at, created_at + interval '2 hour')
where expected_settlement_at is null;

alter table exchange_requests add column if not exists manual_review_required boolean default false;
alter table exchange_requests alter column manual_review_required set not null;

alter table exchange_requests add column if not exists manual_review_reason varchar(255);
