CREATE TABLE strategy_config (
  id BIGINT PRIMARY KEY,
  risk_reward DOUBLE PRECISION NOT NULL,
  stop_loss_pct DOUBLE PRECISION NOT NULL,
  min_avg_volume BIGINT NOT NULL,
  min_price DOUBLE PRECISION NOT NULL,
  lookback_days INT NOT NULL,
  momentum_days INT NOT NULL,
  trend_days INT NOT NULL,
  rsi_days INT NOT NULL,
  volatility_days INT NOT NULL,
  volume_spike_multiplier DOUBLE PRECISION NOT NULL
);

INSERT INTO strategy_config (
  id, risk_reward, stop_loss_pct, min_avg_volume, min_price,
  lookback_days, momentum_days, trend_days, rsi_days, volatility_days,
  volume_spike_multiplier
) VALUES (
  1, 2.0, 0.07, 500000, 5.0,
  252, 20, 60, 14, 20,
  1.5
);
