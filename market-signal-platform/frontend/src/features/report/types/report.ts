export type SignalAction = "BUY" | "WATCH" | "AVOID";
export type MarketRegime = "GROWTH" | "ENERGY" | "RISK_OFF" | "NEUTRAL";

export type SignalSummary = {
  ticker: string;
  score: number;
  action: SignalAction;
  reasons: string[];
};

export type DailyReport = {
  reportDate: string;
  snapshotDate: string;
  generatedAt: string;
  marketRegime: MarketRegime;
  leadingSectors: string[];
  topSignals: SignalSummary[];
  summary: string;
};
