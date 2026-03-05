alter table exchange_requests add column if not exists request_memo varchar(200);
alter table stock_orders add column if not exists order_memo varchar(200);

update exchange_requests
set request_memo = '해외주식 투자자금 환전'
where request_number = 'FX-DEMO-0001'
  and request_memo is null;

update stock_orders
set order_memo = '미국 기술주 분할 매수'
where order_number = 'ORD-DEMO-0001'
  and order_memo is null;
