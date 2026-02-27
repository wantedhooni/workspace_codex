CREATE TABLE tickers (
  id BIGSERIAL PRIMARY KEY,
  symbol VARCHAR(16) NOT NULL UNIQUE,
  name VARCHAR(200),
  sector VARCHAR(200),
  active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE daily_bars (
  id BIGSERIAL PRIMARY KEY,
  ticker_id BIGINT NOT NULL REFERENCES tickers(id),
  trade_date DATE NOT NULL,
  open DOUBLE PRECISION NOT NULL,
  high DOUBLE PRECISION NOT NULL,
  low DOUBLE PRECISION NOT NULL,
  close DOUBLE PRECISION NOT NULL,
  volume BIGINT NOT NULL,
  UNIQUE (ticker_id, trade_date)
);

CREATE INDEX idx_bars_ticker_date ON daily_bars(ticker_id, trade_date);

CREATE TABLE signals (
  id BIGSERIAL PRIMARY KEY,
  ticker_id BIGINT NOT NULL REFERENCES tickers(id),
  signal_date DATE NOT NULL,
  action VARCHAR(10) NOT NULL,
  score DOUBLE PRECISION NOT NULL,
  entry_price DOUBLE PRECISION NOT NULL,
  stop_loss DOUBLE PRECISION NOT NULL,
  take_profit DOUBLE PRECISION NOT NULL,
  rationale VARCHAR(1000)
);

CREATE INDEX idx_signal_ticker_date ON signals(ticker_id, signal_date);

CREATE TABLE backtest_runs (
  id BIGSERIAL PRIMARY KEY,
  strategy_name VARCHAR(100) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  total_return_pct DOUBLE PRECISION NOT NULL,
  max_drawdown_pct DOUBLE PRECISION NOT NULL,
  win_rate_pct DOUBLE PRECISION NOT NULL,
  trades INT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE backtest_trades (
  id BIGSERIAL PRIMARY KEY,
  run_id BIGINT NOT NULL REFERENCES backtest_runs(id) ON DELETE CASCADE,
  ticker_id BIGINT NOT NULL REFERENCES tickers(id),
  entry_date DATE NOT NULL,
  exit_date DATE NOT NULL,
  entry_price DOUBLE PRECISION NOT NULL,
  exit_price DOUBLE PRECISION NOT NULL,
  pnl_pct DOUBLE PRECISION NOT NULL,
  exit_reason VARCHAR(50) NOT NULL
);

CREATE INDEX idx_backtest_trades_run ON backtest_trades(run_id);
