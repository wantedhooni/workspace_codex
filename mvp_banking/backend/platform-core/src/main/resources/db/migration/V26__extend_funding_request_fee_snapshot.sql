alter table funding_requests add column if not exists service_fee_amount numeric(19, 4) not null default 0;
alter table funding_requests add column if not exists total_debit_amount numeric(19, 4) not null default 0;

update funding_requests
set total_debit_amount = amount
where total_debit_amount = 0;
