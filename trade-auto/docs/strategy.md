# Strategy Logic (MVP)

Strategy parameters are stored in DB (`strategy_config`) and can be updated via `PUT /api/v1/strategy`.

## Filters
- Minimum price: `minPrice` (default 5.0)
- Minimum 20D average volume: `minAvgVolume` (default 500k)

## Indicators
- SMA 20, SMA 60
- RSI 14
- Volatility: 20D std dev of daily returns
- Momentum: 20D price change
- Volume spike: last volume > 1.5x 20D avg

## Scoring
- +2 if price above 60D SMA, -1 if below
- +1 if price above 20D SMA
- +2 if momentum > 10%, +1 if > 5%, -1 if < -5%
- +1 if RSI in 50–70, -1 if RSI > 80 or < 30
- +1 if volume spike
- -1 if volatility > 5%

## Action
- BUY if score >= 4
- SELL if score <= 1 and price < 60D SMA
- HOLD otherwise

## Risk
- Stop loss: `max(7% fixed, 1.5 * ATR)`
- Take profit: `risk * 2`
