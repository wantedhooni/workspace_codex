alter table exchange_requests add column if not exists exchange_fee_amount numeric(19, 4);
alter table exchange_requests add column if not exists net_to_amount numeric(19, 4);

update exchange_requests
set exchange_fee_amount = round((to_amount * 0.0012)::numeric, 4),
    net_to_amount = round((to_amount - round((to_amount * 0.0012)::numeric, 4))::numeric, 4)
where exchange_fee_amount is null
   or net_to_amount is null;

alter table exchange_requests alter column exchange_fee_amount set not null;
alter table exchange_requests alter column net_to_amount set not null;

alter table stock_orders add column if not exists fee_amount numeric(19, 4) default 0;
alter table stock_orders add column if not exists tax_amount numeric(19, 4) default 0;
alter table stock_orders add column if not exists net_settlement_amount numeric(19, 4) default 0;

update stock_orders
set fee_amount = case
        when coalesce(executed_quantity, 0) > 0 and coalesce(executed_price, 0) > 0
            then round(((executed_quantity * executed_price) * 0.0015)::numeric, 4)
        else 0
    end,
    tax_amount = case
        when side = 'SELL' and coalesce(executed_quantity, 0) > 0 and coalesce(executed_price, 0) > 0
            then round(((executed_quantity * executed_price) * 0.0023)::numeric, 4)
        else 0
    end,
    net_settlement_amount = case
        when coalesce(executed_quantity, 0) > 0 and coalesce(executed_price, 0) > 0 and side = 'BUY'
            then round((((executed_quantity * executed_price) + round(((executed_quantity * executed_price) * 0.0015)::numeric, 4)))::numeric, 4)
        when coalesce(executed_quantity, 0) > 0 and coalesce(executed_price, 0) > 0 and side = 'SELL'
            then round((((executed_quantity * executed_price)
                - round(((executed_quantity * executed_price) * 0.0015)::numeric, 4)
                - round(((executed_quantity * executed_price) * 0.0023)::numeric, 4)))::numeric, 4)
        else 0
    end
where fee_amount is null
   or tax_amount is null
   or net_settlement_amount is null;

alter table stock_orders alter column fee_amount set not null;
alter table stock_orders alter column tax_amount set not null;
alter table stock_orders alter column net_settlement_amount set not null;
