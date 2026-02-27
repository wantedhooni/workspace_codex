-- V1: quant core schema (order/trade/position/ledger/journal)

create table if not exists users (
    id bigserial primary key,
    email varchar(120) not null unique,
    name varchar(80) not null,
    status varchar(20) not null default 'ACTIVE',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists portfolios (
    id bigserial primary key,
    code varchar(40) not null unique,
    name varchar(120) not null,
    currency varchar(10) not null default 'USD',
    created_at timestamptz not null default now()
);

create table if not exists orders (
    id bigserial primary key,
    portfolio_id bigint not null references portfolios(id),
    symbol varchar(20) not null,
    side varchar(10) not null,
    order_type varchar(20) not null,
    quantity numeric(20,6) not null,
    limit_price numeric(20,6),
    status varchar(20) not null,
    broker_order_id varchar(80),
    trade_date date not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists idx_orders_portfolio_date on orders(portfolio_id, trade_date);
create index if not exists idx_orders_symbol_date on orders(symbol, trade_date);
create index if not exists idx_orders_status on orders(status);

create table if not exists trades (
    id bigserial primary key,
    order_id bigint not null references orders(id),
    broker_trade_id varchar(80) not null,
    symbol varchar(20) not null,
    side varchar(10) not null,
    trade_price numeric(20,6) not null,
    trade_qty numeric(20,6) not null,
    fee_amount numeric(20,6) not null default 0,
    trade_status varchar(20) not null,
    traded_at timestamptz not null,
    created_at timestamptz not null default now(),
    unique (broker_trade_id)
);

create index if not exists idx_trades_order on trades(order_id);
create index if not exists idx_trades_symbol_time on trades(symbol, traded_at);

create table if not exists positions (
    id bigserial primary key,
    portfolio_id bigint not null references portfolios(id),
    symbol varchar(20) not null,
    quantity numeric(20,6) not null,
    avg_price numeric(20,6) not null,
    realized_pnl numeric(20,6) not null default 0,
    unrealized_pnl numeric(20,6) not null default 0,
    as_of_date date not null,
    created_at timestamptz not null default now(),
    unique (portfolio_id, symbol, as_of_date)
);

create index if not exists idx_positions_portfolio_date on positions(portfolio_id, as_of_date);

create table if not exists journal_vouchers (
    id bigserial primary key,
    voucher_no varchar(40) not null unique,
    portfolio_id bigint not null references portfolios(id),
    voucher_date date not null,
    status varchar(20) not null,
    approved_by bigint references users(id),
    approved_at timestamptz,
    posted_at timestamptz,
    description varchar(300),
    created_at timestamptz not null default now()
);

create table if not exists journal_entries (
    id bigserial primary key,
    voucher_id bigint not null references journal_vouchers(id),
    line_no int not null,
    account_code varchar(30) not null,
    dr_cr char(2) not null, -- DR or CR
    amount numeric(20,6) not null,
    symbol varchar(20),
    trade_id bigint references trades(id),
    description varchar(300),
    created_at timestamptz not null default now(),
    unique (voucher_id, line_no)
);

create table if not exists ledger_entries (
    id bigserial primary key,
    account_code varchar(30) not null,
    portfolio_id bigint not null references portfolios(id),
    entry_date date not null,
    dr_cr char(2) not null, -- DR or CR
    amount numeric(20,6) not null,
    voucher_id bigint references journal_vouchers(id),
    entry_ref varchar(80),
    created_at timestamptz not null default now()
);

create index if not exists idx_ledger_account_date on ledger_entries(account_code, entry_date);
create index if not exists idx_ledger_portfolio_date on ledger_entries(portfolio_id, entry_date);
