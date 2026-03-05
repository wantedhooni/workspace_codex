alter table funding_requests add column if not exists daily_limit_amount numeric(19, 4);
alter table funding_requests add column if not exists daily_accumulated_amount numeric(19, 4);
alter table funding_requests add column if not exists daily_limit_exceeded boolean not null default false;
alter table funding_requests add column if not exists same_day_settlement_eligible boolean not null default true;
alter table funding_requests add column if not exists expected_settlement_at timestamp with time zone;
alter table funding_requests add column if not exists manual_review_required boolean not null default false;
alter table funding_requests add column if not exists manual_review_reason varchar(255);
