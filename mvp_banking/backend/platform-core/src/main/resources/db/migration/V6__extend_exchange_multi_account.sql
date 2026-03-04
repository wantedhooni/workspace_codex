alter table exchange_requests add column if not exists source_account_id uuid references accounts(id);
alter table exchange_requests add column if not exists source_transaction_number varchar(50);

update exchange_requests er
set source_account_id = matched.account_id
from (
    select legacy.id as exchange_request_id, account_match.id as account_id
    from exchange_requests legacy
    join lateral (
        select account_candidate.id
        from accounts account_candidate
        where account_candidate.customer_id = legacy.customer_id
          and upper(account_candidate.currency) = upper(legacy.from_currency)
          and account_candidate.account_type = 'BANKING'
        order by account_candidate.created_at desc
        limit 1
    ) account_match on true
) matched
where er.id = matched.exchange_request_id
  and er.source_account_id is null;
