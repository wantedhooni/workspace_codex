alter table funding_requests add column if not exists priority_processing boolean not null default false;
alter table funding_requests add column if not exists priority_fee_amount numeric(19, 4) not null default 0;
