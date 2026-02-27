import express from "express";
import yahooFinance from "yahoo-finance2";

const app = express();
const port = Number(process.env.PORT || 4010);
const yf = new yahooFinance();

app.use(express.json());

function badRequest(res, message) {
  return res.status(400).json({ code: "BAD_REQUEST", message });
}

function providerError(res, message) {
  return res.status(500).json({ code: "PROVIDER_ERROR", message });
}

app.get("/health", (_req, res) => {
  res.json({ status: "ok", provider: "yahoo-finance2" });
});

app.get("/api/market/quote", async (req, res) => {
  const symbol = req.query.symbol;
  if (!symbol || typeof symbol !== "string") {
    return badRequest(res, "query parameter 'symbol' is required");
  }

  try {
    const quote = await yf.quote(symbol);
    return res.json({
      symbol: quote.symbol,
      currency: quote.currency,
      regularMarketPrice: quote.regularMarketPrice,
      regularMarketTime: quote.regularMarketTime,
      marketState: quote.marketState
    });
  } catch (err) {
    return providerError(res, `quote fetch failed: ${String(err.message || err)}`);
  }
});

app.get("/api/market/price-bars", async (req, res) => {
  const symbol = req.query.symbol;
  const interval = typeof req.query.interval === "string" ? req.query.interval : "1d";
  const from = req.query.from;
  const to = req.query.to;

  if (!symbol || typeof symbol !== "string") {
    return badRequest(res, "query parameter 'symbol' is required");
  }

  const queryOptions = { interval };
  if (typeof from === "string") queryOptions.period1 = from;
  if (typeof to === "string") queryOptions.period2 = to;

  try {
    const rows = await yf.chart(symbol, queryOptions);
    const bars = (rows?.quotes || []).map((q) => ({
      date: q.date,
      open: q.open,
      high: q.high,
      low: q.low,
      close: q.close,
      volume: q.volume
    }));

    return res.json({
      symbol,
      interval,
      bars
    });
  } catch (err) {
    return providerError(res, `price-bars fetch failed: ${String(err.message || err)}`);
  }
});

app.listen(port, () => {
  // eslint-disable-next-line no-console
  console.log(`[market-data-adapter] listening on :${port}`);
});
