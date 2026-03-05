create or replace function mvp_seed_uuid(seed_text text)
returns uuid
language sql
immutable
as $$
    select (
        substr(hash, 1, 8) || '-' ||
        substr(hash, 9, 4) || '-' ||
        substr(hash, 13, 4) || '-' ||
        substr(hash, 17, 4) || '-' ||
        substr(hash, 21, 12)
    )::uuid
    from (
        select md5(seed_text) as hash
    ) hashed
$$;

insert into admin_users (
    id,
    email,
    password_hash,
    role,
    display_name,
    active,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('admin-user:admin@mvpbanking.local'),
    'admin@mvpbanking.local',
    '$2a$10$BgoraBRRuXV1ft9WuTR7y./N/lDjjFnd2wdFzYV067m1rntvC8vjO',
    'SUPER_ADMIN',
    'Platform Admin',
    true,
    statement_timestamp(),
    statement_timestamp()
where not exists (
    select 1
    from admin_users
    where email = 'admin@mvpbanking.local'
);

insert into end_users (
    id,
    email,
    password_hash,
    full_name,
    active,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('end-user:user@mvpbanking.local'),
    'user@mvpbanking.local',
    '$2a$10$CgL1z9oW47aPFPIRUDXCDO/K8Dl7mgn6z6CWyJgZ3NOUURwF5n//i',
    'MVP User',
    true,
    statement_timestamp(),
    statement_timestamp()
where not exists (
    select 1
    from end_users
    where email = 'user@mvpbanking.local'
);

with numbered_customers as (
    select
        idx,
        'CUST-' || to_char(100000 + idx, 'FM000000') as customer_number,
        case
            when idx = 1 then 'MVP User'
            else 'Demo Customer ' || lpad(idx::text, 3, '0')
            end as full_name,
        case
            when idx = 1 then 'user@mvpbanking.local'
            else 'customer' || lpad(idx::text, 3, '0') || '@mvpbanking.local'
            end as email,
        case
            when idx = 1 then 'ACTIVE'
            when mod(idx, 17) = 0 then 'SUSPENDED'
            when mod(idx, 8) = 0 then 'REVIEW_REQUIRED'
            else 'ACTIVE'
            end as status
    from generate_series(1, 100) as gs(idx)
)
insert into customers (
    id,
    end_user_id,
    customer_number,
    full_name,
    email,
    status,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('customer:' || numbered_customers.customer_number),
    case
        when numbered_customers.idx = 1
            then (select id from end_users where email = 'user@mvpbanking.local')
        else null
        end as end_user_id,
    numbered_customers.customer_number,
    numbered_customers.full_name,
    numbered_customers.email,
    numbered_customers.status,
    statement_timestamp() - (numbered_customers.idx || ' hours')::interval,
    statement_timestamp() - (numbered_customers.idx || ' hours')::interval
from numbered_customers
on conflict do nothing;

update customers
set
    end_user_id = (select id from end_users where email = 'user@mvpbanking.local'),
    full_name = 'MVP User',
    email = 'user@mvpbanking.local',
    status = 'ACTIVE',
    updated_at = statement_timestamp()
where customer_number = 'CUST-100001';

with bank_accounts as (
    select
        idx,
        'CUST-' || to_char(100000 + idx, 'FM000000') as customer_number,
        '110-' || lpad(idx::text, 3, '0') || '-' || lpad((idx * 13)::text, 6, '0') as account_number,
        case
            when mod(idx, 10) = 0 then 'PENDING_APPROVAL'
            when mod(idx, 21) = 0 then 'LOCKED'
            else 'ACTIVE'
            end as account_status,
        (12500000 + idx * 325000)::numeric(19, 4) as balance
    from generate_series(1, 100) as gs(idx)
)
insert into accounts (
    id,
    customer_id,
    account_number,
    account_type,
    status,
    balance,
    currency,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('account:banking:' || bank_accounts.account_number),
    customers.id,
    bank_accounts.account_number,
    'BANKING',
    bank_accounts.account_status,
    bank_accounts.balance,
    'KRW',
    statement_timestamp() - (bank_accounts.idx || ' hours')::interval,
    statement_timestamp() - (bank_accounts.idx || ' hours')::interval
from bank_accounts
join customers on customers.customer_number = bank_accounts.customer_number
on conflict (account_number) do nothing;

with securities_accounts as (
    select
        idx,
        'CUST-' || to_char(100000 + idx, 'FM000000') as customer_number,
        '800-' || lpad(idx::text, 3, '0') || '-' || lpad((idx * 29)::text, 6, '0') as account_number,
        case
            when mod(idx, 12) = 0 then 'PENDING_APPROVAL'
            when mod(idx, 18) = 0 then 'LOCKED'
            else 'ACTIVE'
            end as account_status,
        (48000000 + idx * 1150000)::numeric(19, 4) as balance
    from generate_series(1, 100) as gs(idx)
    where idx = 1 or mod(idx, 2) = 0
)
insert into accounts (
    id,
    customer_id,
    account_number,
    account_type,
    status,
    balance,
    currency,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('account:securities:' || securities_accounts.account_number),
    customers.id,
    securities_accounts.account_number,
    'SECURITIES',
    securities_accounts.account_status,
    securities_accounts.balance,
    'KRW',
    statement_timestamp() - (securities_accounts.idx || ' hours')::interval + interval '5 minute',
    statement_timestamp() - (securities_accounts.idx || ' hours')::interval + interval '5 minute'
from securities_accounts
join customers on customers.customer_number = securities_accounts.customer_number
on conflict (account_number) do nothing;

with fx_bank_accounts as (
    select
        idx,
        'CUST-' || to_char(100000 + idx, 'FM000000') as customer_number,
        '120-' || lpad(idx::text, 3, '0') || '-' || lpad((idx * 17)::text, 6, '0') as account_number,
        (18000 + idx * 175)::numeric(19, 4) as balance
    from generate_series(1, 100) as gs(idx)
    where idx = 1 or mod(idx, 5) = 0
)
insert into accounts (
    id,
    customer_id,
    account_number,
    account_type,
    status,
    balance,
    currency,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('account:fx-banking:' || fx_bank_accounts.account_number),
    customers.id,
    fx_bank_accounts.account_number,
    'BANKING',
    'ACTIVE',
    fx_bank_accounts.balance,
    'USD',
    statement_timestamp() - (fx_bank_accounts.idx || ' hours')::interval + interval '10 minute',
    statement_timestamp() - (fx_bank_accounts.idx || ' hours')::interval + interval '10 minute'
from fx_bank_accounts
join customers on customers.customer_number = fx_bank_accounts.customer_number
on conflict (account_number) do nothing;

with banking_tx_seed as (
    select
        idx,
        sequence_no,
        'TXN-BNK-' || lpad(idx::text, 3, '0') || '-' || lpad(sequence_no::text, 2, '0') as transaction_number,
        case sequence_no
            when 1 then 'DEPOSIT'
            when 2 then 'WITHDRAWAL'
            else 'DEPOSIT'
            end as transaction_type,
        case
            when sequence_no = 2 and mod(idx, 13) = 0 then 'REJECTED'
            when sequence_no = 3 and mod(idx, 7) = 0 then 'PENDING'
            else 'COMPLETED'
            end as transaction_status,
        (350000 + idx * 25000 + sequence_no * 10000)::numeric(19, 4) as amount
    from generate_series(1, 100) as customer(idx)
    cross join generate_series(1, 3) as tx(sequence_no)
),
banking_accounts as (
    select
        idx,
        accounts.id as account_id,
        accounts.currency
    from generate_series(1, 100) as gs(idx)
    join accounts on accounts.account_number = '110-' || lpad(idx::text, 3, '0') || '-' || lpad((idx * 13)::text, 6, '0')
)
insert into transactions (
    id,
    account_id,
    transaction_number,
    transaction_type,
    status,
    amount,
    currency,
    occurred_at,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('transaction:banking:' || banking_tx_seed.transaction_number),
    banking_accounts.account_id,
    banking_tx_seed.transaction_number,
    banking_tx_seed.transaction_type,
    banking_tx_seed.transaction_status,
    banking_tx_seed.amount,
    banking_accounts.currency,
    statement_timestamp() - (banking_tx_seed.idx || ' hours')::interval - (banking_tx_seed.sequence_no * interval '15 minute'),
    statement_timestamp() - (banking_tx_seed.idx || ' hours')::interval - (banking_tx_seed.sequence_no * interval '15 minute'),
    statement_timestamp() - (banking_tx_seed.idx || ' hours')::interval - (banking_tx_seed.sequence_no * interval '15 minute')
from banking_tx_seed
join banking_accounts on banking_accounts.idx = banking_tx_seed.idx
on conflict (transaction_number) do nothing;

with securities_tx_seed as (
    select
        idx,
        sequence_no,
        'TXN-SEC-' || lpad(idx::text, 3, '0') || '-' || lpad(sequence_no::text, 2, '0') as transaction_number,
        case sequence_no
            when 1 then 'BUY'
            else 'SELL'
            end as transaction_type,
        case
            when sequence_no = 2 and mod(idx, 13) = 0 then 'REJECTED'
            else 'COMPLETED'
            end as transaction_status,
        (1250000 + idx * 25000 + sequence_no * 10000)::numeric(19, 4) as amount
    from generate_series(1, 100) as customer(idx)
    cross join generate_series(1, 2) as tx(sequence_no)
    where idx = 1 or mod(idx, 2) = 0
),
securities_accounts as (
    select
        idx,
        accounts.id as account_id,
        accounts.currency
    from generate_series(1, 100) as gs(idx)
    join accounts on accounts.account_number = '800-' || lpad(idx::text, 3, '0') || '-' || lpad((idx * 29)::text, 6, '0')
)
insert into transactions (
    id,
    account_id,
    transaction_number,
    transaction_type,
    status,
    amount,
    currency,
    occurred_at,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('transaction:securities:' || securities_tx_seed.transaction_number),
    securities_accounts.account_id,
    securities_tx_seed.transaction_number,
    securities_tx_seed.transaction_type,
    securities_tx_seed.transaction_status,
    securities_tx_seed.amount,
    securities_accounts.currency,
    statement_timestamp() - (securities_tx_seed.idx || ' hours')::interval - (securities_tx_seed.sequence_no * interval '10 minute'),
    statement_timestamp() - (securities_tx_seed.idx || ' hours')::interval - (securities_tx_seed.sequence_no * interval '10 minute'),
    statement_timestamp() - (securities_tx_seed.idx || ' hours')::interval - (securities_tx_seed.sequence_no * interval '10 minute')
from securities_tx_seed
join securities_accounts on securities_accounts.idx = securities_tx_seed.idx
on conflict (transaction_number) do nothing;

insert into approval_requests (
    id,
    target_type,
    target_id,
    title,
    description,
    status,
    requested_by_email,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('approval:withdrawal-review:' || accounts.account_number),
    'WITHDRAWAL',
    accounts.id,
    'Withdrawal review for ' || customers.customer_number,
    'Manual review required for ' || accounts.account_number,
    'PENDING',
    'ops.queue@mvpbanking.local',
    statement_timestamp() - interval '8 hour',
    statement_timestamp() - interval '8 hour'
from accounts
join customers on customers.id = accounts.customer_id
where accounts.account_type = 'BANKING'
  and accounts.status = 'PENDING_APPROVAL'
  and not exists (
    select 1
    from approval_requests existing
    where existing.title = 'Withdrawal review for ' || customers.customer_number
);

insert into approval_requests (
    id,
    target_type,
    target_id,
    title,
    description,
    status,
    requested_by_email,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('approval:securities-activation:' || accounts.account_number),
    'ACCOUNT',
    accounts.id,
    'Account activation review for ' || customers.customer_number,
    'Pending securities account activation for ' || accounts.account_number,
    'PENDING',
    'ops.queue@mvpbanking.local',
    statement_timestamp() - interval '8 hour',
    statement_timestamp() - interval '8 hour'
from accounts
join customers on customers.id = accounts.customer_id
where accounts.account_type = 'SECURITIES'
  and accounts.status = 'PENDING_APPROVAL'
  and not exists (
    select 1
    from approval_requests existing
    where existing.title = 'Account activation review for ' || customers.customer_number
);

with fx_seed as (
    select * from (
        values
            ('USD', 'KRW', 1338.250000::numeric(19, 6)),
            ('JPY', 'KRW', 8.964000::numeric(19, 6)),
            ('EUR', 'KRW', 1451.820000::numeric(19, 6)),
            ('KRW', 'USD', 0.000747::numeric(19, 6))
    ) as seed(base_currency, quote_currency, rate)
)
insert into fx_rates (
    id,
    base_currency,
    quote_currency,
    rate,
    effective_at,
    source,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('fx-rate:' || fx_seed.base_currency || ':' || fx_seed.quote_currency),
    fx_seed.base_currency,
    fx_seed.quote_currency,
    fx_seed.rate,
    statement_timestamp() - interval '10 minute',
    'MVP_MARKET_DATA',
    statement_timestamp() - interval '10 minute',
    statement_timestamp() - interval '10 minute'
from fx_seed
where not exists (
    select 1
    from fx_rates existing
    where existing.base_currency = fx_seed.base_currency
      and existing.quote_currency = fx_seed.quote_currency
);

with primary_customer as (
    select id, email
    from customers
    where customer_number = 'CUST-100001'
),
linked_account_seed as (
    select * from (
        values
            (
                'Shinhan Bank',
                '급여 출금 계좌',
                'MVP User',
                '110-999-123456',
                'ACTIVE',
                true,
                statement_timestamp() - interval '2 day',
                null::varchar(20),
                null::timestamp with time zone,
                0,
                null::timestamp with time zone,
                null::varchar(50),
                null::timestamp with time zone
            ),
            (
                'KB Kookmin Bank',
                '예비 생활비 계좌',
                'MVP User',
                '004-555-987654',
                'ACTIVE',
                false,
                statement_timestamp() - interval '1 day',
                null::varchar(20),
                null::timestamp with time zone,
                0,
                null::timestamp with time zone,
                null::varchar(50),
                null::timestamp with time zone
            ),
            (
                'Toss Bank',
                '신규 출금 계좌',
                'MVP User',
                '100-321-654987',
                'PENDING_VERIFICATION',
                false,
                null::timestamp with time zone,
                'MVP-2468',
                statement_timestamp() - interval '5 minute',
                0,
                null::timestamp with time zone,
                null::varchar(50),
                null::timestamp with time zone
            )
    ) as seed(
        bank_name,
        account_alias,
        account_holder_name,
        account_number,
        account_status,
        primary_withdrawal,
        verified_at,
        verification_reference,
        verification_requested_at,
        verification_attempt_count,
        last_verification_resent_at,
        block_reason_code,
        blocked_at
    )
)
insert into linked_bank_accounts (
    id,
    customer_id,
    customer_email,
    bank_name,
    account_alias,
    account_holder_name,
    account_number,
    status,
    primary_withdrawal,
    verified_at,
    verification_reference,
    verification_requested_at,
    verification_attempt_count,
    last_verification_resent_at,
    block_reason_code,
    blocked_at,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('linked-bank-account:' || linked_account_seed.account_number),
    primary_customer.id,
    primary_customer.email,
    linked_account_seed.bank_name,
    linked_account_seed.account_alias,
    linked_account_seed.account_holder_name,
    linked_account_seed.account_number,
    linked_account_seed.account_status,
    linked_account_seed.primary_withdrawal,
    linked_account_seed.verified_at,
    linked_account_seed.verification_reference,
    linked_account_seed.verification_requested_at,
    linked_account_seed.verification_attempt_count,
    linked_account_seed.last_verification_resent_at,
    linked_account_seed.block_reason_code,
    linked_account_seed.blocked_at,
    statement_timestamp() - interval '12 hour',
    statement_timestamp() - interval '12 hour'
from primary_customer
cross join linked_account_seed
where not exists (
    select 1
    from linked_bank_accounts existing
    where existing.customer_id = primary_customer.id
      and existing.account_number = linked_account_seed.account_number
);

with primary_customer as (
    select id, email
    from customers
    where customer_number = 'CUST-100001'
),
krw_banking_account as (
    select id, account_number, account_type, balance, currency
    from accounts
    where account_number = '110-001-000013'
),
primary_linked_account as (
    select
        id,
        bank_name,
        account_alias,
        account_holder_name
    from linked_bank_accounts
    where account_number = '110-999-123456'
)
insert into funding_requests (
    id,
    customer_id,
    customer_email,
    account_id,
    account_number,
    account_type,
    request_number,
    request_type,
    status,
    amount,
    currency,
    balance_snapshot,
    service_fee_amount,
    priority_processing,
    priority_fee_amount,
    total_debit_amount,
    linked_bank_account_id,
    linked_bank_name,
    linked_bank_account_alias,
    linked_bank_account_number_masked,
    linked_bank_account_holder_name,
    daily_limit_amount,
    daily_accumulated_amount,
    daily_limit_exceeded,
    same_day_settlement_eligible,
    expected_settlement_at,
    manual_review_required,
    manual_review_reason,
    note,
    settlement_transaction_number,
    settled_at,
    cancellation_reason,
    canceled_at,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('funding-request:FND-DEMO-0001'),
    primary_customer.id,
    primary_customer.email,
    krw_banking_account.id,
    krw_banking_account.account_number,
    krw_banking_account.account_type,
    'FND-DEMO-0001',
    'WITHDRAWAL',
    'PENDING_APPROVAL',
    1250000.0000::numeric(19, 4),
    krw_banking_account.currency,
    krw_banking_account.balance,
    1000.0000::numeric(19, 4),
    false,
    0.0000::numeric(19, 4),
    1251000.0000::numeric(19, 4),
    primary_linked_account.id,
    primary_linked_account.bank_name,
    primary_linked_account.account_alias,
    '***-***-3456',
    primary_linked_account.account_holder_name,
    3000000.0000::numeric(19, 4),
    1250000.0000::numeric(19, 4),
    false,
    true,
    statement_timestamp() + interval '6 hour',
    true,
    '고액 출금 심사',
    '월간 운영비 출금 요청',
    null,
    null,
    null,
    null,
    statement_timestamp() - interval '4 hour',
    statement_timestamp() - interval '4 hour'
from primary_customer
join krw_banking_account on true
join primary_linked_account on true
where not exists (
    select 1
    from funding_requests
    where request_number = 'FND-DEMO-0001'
);

with primary_customer as (
    select id, email
    from customers
    where customer_number = 'CUST-100001'
),
securities_account as (
    select id, account_number, account_type, balance, currency
    from accounts
    where account_number = '800-001-000029'
)
insert into funding_requests (
    id,
    customer_id,
    customer_email,
    account_id,
    account_number,
    account_type,
    request_number,
    request_type,
    status,
    amount,
    currency,
    balance_snapshot,
    service_fee_amount,
    priority_processing,
    priority_fee_amount,
    total_debit_amount,
    linked_bank_account_id,
    linked_bank_name,
    linked_bank_account_alias,
    linked_bank_account_number_masked,
    linked_bank_account_holder_name,
    daily_limit_amount,
    daily_accumulated_amount,
    daily_limit_exceeded,
    same_day_settlement_eligible,
    expected_settlement_at,
    manual_review_required,
    manual_review_reason,
    note,
    settlement_transaction_number,
    settled_at,
    cancellation_reason,
    canceled_at,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('funding-request:FND-DEMO-0002'),
    primary_customer.id,
    primary_customer.email,
    securities_account.id,
    securities_account.account_number,
    securities_account.account_type,
    'FND-DEMO-0002',
    'DEPOSIT',
    'APPROVED',
    3500000.0000::numeric(19, 4),
    securities_account.currency,
    securities_account.balance,
    0.0000::numeric(19, 4),
    false,
    0.0000::numeric(19, 4),
    3500000.0000::numeric(19, 4),
    null,
    null,
    null,
    null,
    null,
    20000000.0000::numeric(19, 4),
    3500000.0000::numeric(19, 4),
    false,
    true,
    statement_timestamp() + interval '4 hour',
    false,
    null,
    '해외주식 추가 매수용 예탁금 입금',
    'TXN-FND-FND-DEMO-0002',
    statement_timestamp() - interval '3 hour',
    null,
    null,
    statement_timestamp() - interval '6 hour',
    statement_timestamp() - interval '3 hour'
from primary_customer
join securities_account on true
where not exists (
    select 1
    from funding_requests
    where request_number = 'FND-DEMO-0002'
);

with funding_deposit as (
    select account_id, currency
    from funding_requests
    where request_number = 'FND-DEMO-0002'
)
insert into transactions (
    id,
    account_id,
    transaction_number,
    transaction_type,
    status,
    amount,
    currency,
    occurred_at,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('transaction:TXN-FND-FND-DEMO-0002'),
    funding_deposit.account_id,
    'TXN-FND-FND-DEMO-0002',
    'DEPOSIT',
    'COMPLETED',
    3500000.0000::numeric(19, 4),
    funding_deposit.currency,
    statement_timestamp() - interval '3 hour',
    statement_timestamp() - interval '3 hour',
    statement_timestamp() - interval '3 hour'
from funding_deposit
where not exists (
    select 1
    from transactions
    where transaction_number = 'TXN-FND-FND-DEMO-0002'
);

insert into approval_requests (
    id,
    target_type,
    target_id,
    title,
    description,
    status,
    requested_by_email,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('approval:funding:FND-DEMO-0001'),
    'FUNDING_REQUEST',
    funding_requests.id,
    'WITHDRAWAL funding FND-DEMO-0001',
    'WITHDRAWAL 1250000.0000 KRW',
    'PENDING',
    funding_requests.customer_email,
    statement_timestamp() - interval '4 hour',
    statement_timestamp() - interval '4 hour'
from funding_requests
where funding_requests.request_number = 'FND-DEMO-0001'
  and not exists (
    select 1
    from approval_requests
    where title = 'WITHDRAWAL funding FND-DEMO-0001'
);

with primary_customer as (
    select id, email
    from customers
    where customer_number = 'CUST-100001'
),
source_account as (
    select id
    from accounts
    where account_number = '120-001-000017'
),
destination_account as (
    select id
    from accounts
    where account_number = '110-001-000013'
)
insert into exchange_requests (
    id,
    customer_id,
    account_id,
    source_account_id,
    request_number,
    from_currency,
    to_currency,
    from_amount,
    applied_rate,
    applied_rate_effective_at,
    to_amount,
    exchange_fee_amount,
    net_to_amount,
    status,
    source_transaction_number,
    settlement_transaction_number,
    request_memo,
    same_day_settlement_eligible,
    expected_settlement_at,
    manual_review_required,
    manual_review_reason,
    cancellation_reason,
    canceled_at,
    settled_at,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('exchange-request:FX-DEMO-0001'),
    primary_customer.id,
    destination_account.id,
    source_account.id,
    'FX-DEMO-0001',
    'USD',
    'KRW',
    2500.0000::numeric(19, 4),
    1338.250000::numeric(19, 6),
    statement_timestamp() - interval '10 minute',
    3345625.0000::numeric(19, 4),
    4014.7500::numeric(19, 4),
    3341610.2500::numeric(19, 4),
    'PENDING_APPROVAL',
    null,
    null,
    '해외주식 투자자금 환전',
    true,
    statement_timestamp() + interval '2 hour',
    false,
    null,
    null,
    null,
    null,
    statement_timestamp() - interval '3 hour',
    statement_timestamp() - interval '3 hour'
from primary_customer
join source_account on true
join destination_account on true
where not exists (
    select 1
    from exchange_requests
    where request_number = 'FX-DEMO-0001'
);

insert into approval_requests (
    id,
    target_type,
    target_id,
    title,
    description,
    status,
    requested_by_email,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('approval:exchange:FX-DEMO-0001'),
    'FX_EXCHANGE',
    exchange_requests.id,
    'FX exchange FX-DEMO-0001',
    'USD to KRW exchange request',
    'PENDING',
    customers.email,
    statement_timestamp() - interval '3 hour',
    statement_timestamp() - interval '3 hour'
from exchange_requests
join customers on customers.id = exchange_requests.customer_id
where exchange_requests.request_number = 'FX-DEMO-0001'
  and not exists (
    select 1
    from approval_requests
    where title = 'FX exchange FX-DEMO-0001'
);

with quote_seed as (
    select * from (
        values
            ('AAPL', 'NASDAQ', 189.4000::numeric(19, 4), 'USD', 0.012500::numeric(19, 6)),
            ('MSFT', 'NASDAQ', 332.8000::numeric(19, 4), 'USD', 0.008200::numeric(19, 6)),
            ('TSLA', 'NASDAQ', 241.7000::numeric(19, 4), 'USD', -0.006500::numeric(19, 6))
    ) as seed(symbol, market, price, currency, change_rate)
)
insert into stock_quotes (
    id,
    symbol,
    market,
    price,
    currency,
    change_rate,
    effective_at,
    source,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('stock-quote:' || quote_seed.symbol || ':' || quote_seed.market),
    quote_seed.symbol,
    quote_seed.market,
    quote_seed.price,
    quote_seed.currency,
    quote_seed.change_rate,
    statement_timestamp() - interval '5 minute',
    'MVP_MARKET_DATA',
    statement_timestamp() - interval '5 minute',
    statement_timestamp() - interval '5 minute'
from quote_seed
where not exists (
    select 1
    from stock_quotes existing
    where upper(existing.symbol) = upper(quote_seed.symbol)
      and upper(existing.market) = upper(quote_seed.market)
);

with primary_customer as (
    select id
    from customers
    where customer_number = 'CUST-100001'
),
securities_account as (
    select id
    from accounts
    where account_number = '800-001-000029'
)
insert into stock_orders (
    id,
    customer_id,
    account_id,
    order_number,
    symbol,
    market,
    side,
    quantity,
    limit_price,
    gross_amount,
    currency,
    status,
    executed_quantity,
    executed_price,
    remaining_quantity,
    fee_amount,
    tax_amount,
    net_settlement_amount,
    settlement_transaction_number,
    order_memo,
    time_in_force,
    expires_at,
    market_session,
    expected_execution_at,
    manual_review_required,
    manual_review_reason,
    reference_price,
    price_deviation_rate,
    quote_effective_at,
    quote_source,
    cancellation_reason,
    canceled_at,
    settled_at,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('stock-order:ORD-DEMO-0001'),
    primary_customer.id,
    securities_account.id,
    'ORD-DEMO-0001',
    'AAPL',
    'NASDAQ',
    'BUY',
    10.0000::numeric(19, 4),
    182.5000::numeric(19, 4),
    1825.0000::numeric(19, 4),
    'USD',
    'PENDING_APPROVAL',
    null,
    null,
    10.0000::numeric(19, 4),
    0.0000::numeric(19, 4),
    0.0000::numeric(19, 4),
    0.0000::numeric(19, 4),
    null,
    '미국 기술주 분할 매수',
    'DAY',
    statement_timestamp() + interval '1 day',
    'REGULAR',
    statement_timestamp() + interval '5 minute',
    false,
    null,
    189.4000::numeric(19, 4),
    -0.036431::numeric(19, 6),
    statement_timestamp() - interval '5 minute',
    'MVP_MARKET_DATA',
    null,
    null,
    null,
    statement_timestamp() - interval '2 hour',
    statement_timestamp() - interval '2 hour'
from primary_customer
join securities_account on true
where not exists (
    select 1
    from stock_orders
    where order_number = 'ORD-DEMO-0001'
);

with primary_customer as (
    select id
    from customers
    where customer_number = 'CUST-100001'
),
securities_account as (
    select id
    from accounts
    where account_number = '800-001-000029'
)
insert into stock_orders (
    id,
    customer_id,
    account_id,
    order_number,
    symbol,
    market,
    side,
    quantity,
    limit_price,
    gross_amount,
    currency,
    status,
    executed_quantity,
    executed_price,
    remaining_quantity,
    fee_amount,
    tax_amount,
    net_settlement_amount,
    settlement_transaction_number,
    order_memo,
    time_in_force,
    expires_at,
    market_session,
    expected_execution_at,
    manual_review_required,
    manual_review_reason,
    reference_price,
    price_deviation_rate,
    quote_effective_at,
    quote_source,
    cancellation_reason,
    canceled_at,
    settled_at,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('stock-order:ORD-DEMO-EXEC-0001'),
    primary_customer.id,
    securities_account.id,
    'ORD-DEMO-EXEC-0001',
    'TSLA',
    'NASDAQ',
    'BUY',
    6.0000::numeric(19, 4),
    240.5000::numeric(19, 4),
    1443.0000::numeric(19, 4),
    'USD',
    'APPROVED',
    6.0000::numeric(19, 4),
    240.5000::numeric(19, 4),
    0.0000::numeric(19, 4),
    2.1645::numeric(19, 4),
    0.0000::numeric(19, 4),
    1445.1645::numeric(19, 4),
    'TXN-STK-ORD-DEMO-EXEC-0001',
    '테슬라 분할 매수 체결',
    'DAY',
    statement_timestamp() + interval '1 day',
    'REGULAR',
    statement_timestamp() - interval '5 hour' + interval '5 minute',
    false,
    null,
    241.7000::numeric(19, 4),
    -0.004968::numeric(19, 6),
    statement_timestamp() - interval '5 hour' - interval '5 minute',
    'MVP_MARKET_DATA',
    null,
    null,
    statement_timestamp() - interval '5 hour',
    statement_timestamp() - interval '6 hour',
    statement_timestamp() - interval '5 hour'
from primary_customer
join securities_account on true
where not exists (
    select 1
    from stock_orders
    where order_number = 'ORD-DEMO-EXEC-0001'
);

with primary_customer as (
    select id
    from customers
    where customer_number = 'CUST-100001'
),
securities_account as (
    select id
    from accounts
    where account_number = '800-001-000029'
)
insert into stock_orders (
    id,
    customer_id,
    account_id,
    order_number,
    symbol,
    market,
    side,
    quantity,
    limit_price,
    gross_amount,
    currency,
    status,
    executed_quantity,
    executed_price,
    remaining_quantity,
    fee_amount,
    tax_amount,
    net_settlement_amount,
    settlement_transaction_number,
    order_memo,
    time_in_force,
    expires_at,
    market_session,
    expected_execution_at,
    manual_review_required,
    manual_review_reason,
    reference_price,
    price_deviation_rate,
    quote_effective_at,
    quote_source,
    cancellation_reason,
    canceled_at,
    settled_at,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('stock-order:ORD-DEMO-PARTIAL-0001'),
    primary_customer.id,
    securities_account.id,
    'ORD-DEMO-PARTIAL-0001',
    'AAPL',
    'NASDAQ',
    'BUY',
    10.0000::numeric(19, 4),
    188.2000::numeric(19, 4),
    1882.0000::numeric(19, 4),
    'USD',
    'PARTIALLY_FILLED',
    7.0000::numeric(19, 4),
    188.2000::numeric(19, 4),
    3.0000::numeric(19, 4),
    1.9761::numeric(19, 4),
    0.0000::numeric(19, 4),
    1319.3761::numeric(19, 4),
    'TXN-STK-ORD-DEMO-PARTIAL-0001',
    '애플 분할 매수 진행',
    'DAY',
    statement_timestamp() + interval '1 day',
    'REGULAR',
    statement_timestamp() - interval '4 hour' + interval '5 minute',
    false,
    null,
    189.4000::numeric(19, 4),
    -0.006336::numeric(19, 6),
    statement_timestamp() - interval '4 hour' - interval '5 minute',
    'MVP_MARKET_DATA',
    null,
    null,
    statement_timestamp() - interval '4 hour',
    statement_timestamp() - interval '5 hour',
    statement_timestamp() - interval '4 hour'
from primary_customer
join securities_account on true
where not exists (
    select 1
    from stock_orders
    where order_number = 'ORD-DEMO-PARTIAL-0001'
);

insert into approval_requests (
    id,
    target_type,
    target_id,
    title,
    description,
    status,
    requested_by_email,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('approval:stock-order:ORD-DEMO-0001'),
    'STOCK_ORDER',
    stock_orders.id,
    'Stock order ORD-DEMO-0001',
    'BUY AAPL on NASDAQ',
    'PENDING',
    customers.email,
    statement_timestamp() - interval '2 hour',
    statement_timestamp() - interval '2 hour'
from stock_orders
join customers on customers.id = stock_orders.customer_id
where stock_orders.order_number = 'ORD-DEMO-0001'
  and not exists (
    select 1
    from approval_requests
    where title = 'Stock order ORD-DEMO-0001'
);

with stock_settlement_seed as (
    select * from (
        values
            ('ORD-DEMO-EXEC-0001', 'TXN-STK-ORD-DEMO-EXEC-0001', 1445.1645::numeric(19, 4), statement_timestamp() - interval '5 hour'),
            ('ORD-DEMO-PARTIAL-0001', 'TXN-STK-ORD-DEMO-PARTIAL-0001', 1319.3761::numeric(19, 4), statement_timestamp() - interval '4 hour')
    ) as seed(order_number, transaction_number, amount, occurred_at)
)
insert into transactions (
    id,
    account_id,
    transaction_number,
    transaction_type,
    status,
    amount,
    currency,
    occurred_at,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('transaction:' || stock_settlement_seed.transaction_number),
    stock_orders.account_id,
    stock_settlement_seed.transaction_number,
    'BUY',
    'COMPLETED',
    stock_settlement_seed.amount,
    stock_orders.currency,
    stock_settlement_seed.occurred_at,
    stock_settlement_seed.occurred_at,
    stock_settlement_seed.occurred_at
from stock_settlement_seed
join stock_orders on stock_orders.order_number = stock_settlement_seed.order_number
where not exists (
    select 1
    from transactions existing
    where existing.transaction_number = stock_settlement_seed.transaction_number
);

with execution_seed as (
    select * from (
        values
            ('ORD-DEMO-EXEC-0001', 'EXE-ORD-DEMO-EXEC-0001-01', 1, 3.0000::numeric(19, 4), 240.3000::numeric(19, 4), 720.9000::numeric(19, 4), statement_timestamp() - interval '5 hour' - interval '10 minute'),
            ('ORD-DEMO-EXEC-0001', 'EXE-ORD-DEMO-EXEC-0001-02', 2, 3.0000::numeric(19, 4), 240.7000::numeric(19, 4), 722.1000::numeric(19, 4), statement_timestamp() - interval '5 hour' - interval '5 minute'),
            ('ORD-DEMO-PARTIAL-0001', 'EXE-ORD-DEMO-PARTIAL-0001-01', 1, 4.0000::numeric(19, 4), 188.2000::numeric(19, 4), 752.8000::numeric(19, 4), statement_timestamp() - interval '4 hour' - interval '7 minute'),
            ('ORD-DEMO-PARTIAL-0001', 'EXE-ORD-DEMO-PARTIAL-0001-02', 2, 3.0000::numeric(19, 4), 188.2000::numeric(19, 4), 564.6000::numeric(19, 4), statement_timestamp() - interval '4 hour' - interval '3 minute')
    ) as seed(order_number, execution_number, execution_sequence, executed_quantity, executed_price, executed_amount, executed_at)
)
insert into stock_order_executions (
    id,
    order_id,
    execution_number,
    execution_sequence,
    executed_quantity,
    executed_price,
    executed_amount,
    executed_at,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('stock-execution:' || execution_seed.execution_number),
    stock_orders.id,
    execution_seed.execution_number,
    execution_seed.execution_sequence,
    execution_seed.executed_quantity,
    execution_seed.executed_price,
    execution_seed.executed_amount,
    execution_seed.executed_at,
    execution_seed.executed_at,
    execution_seed.executed_at
from execution_seed
join stock_orders on stock_orders.order_number = execution_seed.order_number
where not exists (
    select 1
    from stock_order_executions existing
    where existing.execution_number = execution_seed.execution_number
);

with primary_customer as (
    select id
    from customers
    where customer_number = 'CUST-100001'
),
securities_account as (
    select id
    from accounts
    where account_number = '800-001-000029'
),
position_seed as (
    select * from (
        values
            ('MSFT', 'NASDAQ', 12.0000::numeric(19, 4), 318.2500::numeric(19, 4), 0.0000::numeric(19, 4), 'USD'),
            ('AAPL', 'NASDAQ', 7.0000::numeric(19, 4), 188.2000::numeric(19, 4), 0.0000::numeric(19, 4), 'USD'),
            ('TSLA', 'NASDAQ', 6.0000::numeric(19, 4), 240.5000::numeric(19, 4), 0.0000::numeric(19, 4), 'USD')
    ) as seed(symbol, market, quantity, average_price, realized_profit_loss, currency)
)
insert into stock_positions (
    id,
    customer_id,
    account_id,
    symbol,
    market,
    quantity,
    average_price,
    currency,
    realized_profit_loss,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('stock-position:' || position_seed.symbol || ':' || securities_account.id::text),
    primary_customer.id,
    securities_account.id,
    position_seed.symbol,
    position_seed.market,
    position_seed.quantity,
    position_seed.average_price,
    position_seed.currency,
    position_seed.realized_profit_loss,
    statement_timestamp() - interval '5 hour',
    statement_timestamp() - interval '5 hour'
from primary_customer
join securities_account on true
cross join position_seed
where not exists (
    select 1
    from stock_positions existing
    where existing.account_id = securities_account.id
      and upper(existing.symbol) = upper(position_seed.symbol)
);

with announcement_seed as (
    select * from (
        values
            (
                'ANNOUNCEMENT-GLOBAL-MAINTENANCE',
                '해외주식 주문 점검 예정 안내',
                '이번 주말 해외주식 지정가 주문 점검이 예정되어 있습니다.',
                '2026년 3월 8일 02:00부터 05:00까지 해외주식 주문, 정정, 취소 기능이 순차 점검됩니다. 점검 시간에는 주문 체결 조회가 지연될 수 있습니다.',
                'WARNING',
                'ALL',
                'PUBLISHED',
                true,
                statement_timestamp() - interval '1 hour',
                statement_timestamp() + interval '2 day',
                statement_timestamp() - interval '50 minute'
            ),
            (
                'ANNOUNCEMENT-FX-CUTOFF',
                '환전 당일 정산 컷오프 시간 안내',
                'USD/KRW 환전은 영업일 16:00 이후 익영업일 정산될 수 있습니다.',
                '환전 신청 시각이 영업일 16:00 이후인 경우 정산 시간이 익영업일 오전으로 이월될 수 있습니다. 긴급 환전은 운영센터를 통해 별도 문의해 주세요.',
                'INFO',
                'USER',
                'PUBLISHED',
                false,
                statement_timestamp() - interval '2 hour',
                statement_timestamp() + interval '7 day',
                statement_timestamp() - interval '90 minute'
            ),
            (
                'ANNOUNCEMENT-ADMIN-OPS',
                '운영자 공지: 승인 큐 우선 검토',
                '부분체결 주문과 FX 승인 건을 우선 검토해 주세요.',
                '운영자용 공지입니다. 장 시작 전 부분체결 잔여 주문과 전일 16:00 이후 환전 요청을 우선 확인해 주세요.',
                'CRITICAL',
                'ADMIN',
                'PUBLISHED',
                true,
                statement_timestamp() - interval '30 minute',
                statement_timestamp() + interval '1 day',
                statement_timestamp() - interval '20 minute'
            ),
            (
                'ANNOUNCEMENT-DRAFT-POLICY',
                '보안 정책 변경 사전 공지',
                '2단계 인증 옵션 제공을 위한 사전 공지 초안입니다.',
                '다음 배포에서 2단계 인증과 로그인 알림 정책이 추가될 예정입니다. 아직 게시되지 않은 초안입니다.',
                'INFO',
                'ALL',
                'DRAFT',
                false,
                statement_timestamp() + interval '1 day',
                statement_timestamp() + interval '3 day',
                null::timestamp with time zone
            )
    ) as seed(
        announcement_key,
        title,
        summary,
        body,
        severity,
        audience,
        announcement_status,
        pinned,
        starts_at,
        ends_at,
        published_at
    )
)
insert into announcements (
    id,
    announcement_key,
    title,
    summary,
    body,
    severity,
    audience,
    status,
    pinned,
    starts_at,
    ends_at,
    published_at,
    archived_at,
    created_by_email,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('announcement:' || announcement_seed.announcement_key),
    announcement_seed.announcement_key,
    announcement_seed.title,
    announcement_seed.summary,
    announcement_seed.body,
    announcement_seed.severity,
    announcement_seed.audience,
    announcement_seed.announcement_status,
    announcement_seed.pinned,
    announcement_seed.starts_at,
    announcement_seed.ends_at,
    announcement_seed.published_at,
    null,
    'admin@mvpbanking.local',
    statement_timestamp() - interval '1 hour',
    statement_timestamp() - interval '1 hour'
from announcement_seed
on conflict (announcement_key) do nothing;

with admin_user as (
    select id
    from admin_users
    where email = 'admin@mvpbanking.local'
)
insert into notifications (
    id,
    notification_key,
    recipient_type,
    recipient_id,
    category,
    severity,
    title,
    message,
    action_path,
    reference_type,
    reference_id,
    read_at,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('notification:ADMIN-SEED-QUEUE'),
    'ADMIN-SEED-QUEUE',
    'ADMIN',
    admin_user.id,
    'APPROVAL',
    'ACTION_REQUIRED',
    '승인 대기열 점검 필요',
    '승인 SLA 초과 항목과 부분 체결 주문을 확인하세요.',
    '/approvals',
    'APPROVAL_QUEUE',
    null,
    null,
    statement_timestamp() - interval '50 minute',
    statement_timestamp() - interval '50 minute'
from admin_user
where not exists (
    select 1
    from notifications
    where notification_key = 'ADMIN-SEED-QUEUE'
);

with admin_user as (
    select id
    from admin_users
    where email = 'admin@mvpbanking.local'
)
insert into notifications (
    id,
    notification_key,
    recipient_type,
    recipient_id,
    category,
    severity,
    title,
    message,
    action_path,
    reference_type,
    reference_id,
    read_at,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('notification:ADMIN-SEED-MARKET'),
    'ADMIN-SEED-MARKET',
    'ADMIN',
    admin_user.id,
    'SYSTEM',
    'WARNING',
    '시세 freshness 점검',
    'FX 또는 주식 시세 freshness가 기준 시간에 근접했습니다.',
    '/fx-rates',
    'MARKET_DATA',
    null,
    null,
    statement_timestamp() - interval '45 minute',
    statement_timestamp() - interval '45 minute'
from admin_user
where not exists (
    select 1
    from notifications
    where notification_key = 'ADMIN-SEED-MARKET'
);

with end_user as (
    select id
    from end_users
    where email = 'user@mvpbanking.local'
),
user_notification_seed as (
    select * from (
        values
            (
                'PORTFOLIO',
                'INFO',
                '포트폴리오 현황 안내',
                '통화 노출과 상위 보유 종목을 알림센터에서 함께 확인할 수 있습니다.',
                '/notifications',
                'DASHBOARD',
                null::uuid,
                'USER-SEED-PORTFOLIO:'
            ),
            (
                'EXCHANGE',
                'ACTION_REQUIRED',
                '환전 요청 승인 대기',
                'FX-DEMO-0001 요청이 운영 승인 대기 중입니다.',
                '/exchange-requests',
                'EXCHANGE_REQUEST',
                null::uuid,
                'USER-SEED-EXCHANGE:'
            ),
            (
                'FUNDING',
                'INFO',
                '입출금 요청 사용 가능',
                '은행/증권 계좌별 입출금 요청을 새 티켓 화면에서 직접 등록할 수 있습니다.',
                '/funding-requests',
                'FUNDING_REQUEST',
                null::uuid,
                'USER-SEED-FUNDING:'
            ),
            (
                'FUNDING',
                'INFO',
                '출금 연결 계좌 등록 가능',
                '외부 은행 연결 계좌를 등록하면 출금 요청 목적지를 직접 선택할 수 있습니다.',
                '/linked-bank-accounts',
                'LINKED_BANK_ACCOUNT',
                null::uuid,
                'USER-SEED-LINKED-BANK:'
            )
    ) as seed(
        category,
        severity,
        title,
        message,
        action_path,
        reference_type,
        reference_id,
        key_prefix
    )
)
insert into notifications (
    id,
    notification_key,
    recipient_type,
    recipient_id,
    category,
    severity,
    title,
    message,
    action_path,
    reference_type,
    reference_id,
    read_at,
    created_at,
    updated_at
)
select
    mvp_seed_uuid('notification:' || user_notification_seed.key_prefix || end_user.id::text),
    user_notification_seed.key_prefix || end_user.id::text,
    'USER',
    end_user.id,
    user_notification_seed.category,
    user_notification_seed.severity,
    user_notification_seed.title,
    user_notification_seed.message,
    user_notification_seed.action_path,
    user_notification_seed.reference_type,
    user_notification_seed.reference_id,
    null,
    statement_timestamp() - interval '40 minute',
    statement_timestamp() - interval '40 minute'
from end_user
cross join user_notification_seed
where not exists (
    select 1
    from notifications
    where notification_key = user_notification_seed.key_prefix || end_user.id::text
);

drop function if exists mvp_seed_uuid(text);
