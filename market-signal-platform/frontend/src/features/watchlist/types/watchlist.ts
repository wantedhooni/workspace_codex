export type WatchlistItem = {
  id: number;
  ticker: string;
  companyName: string;
  createdAt: string;
};

export type WatchlistCoverage = {
  id: number;
  ticker: string;
  companyName: string;
  createdAt: string;
  signalAvailable: boolean;
  latestSignalDate: string | null;
  score: number | null;
  action: "BUY" | "WATCH" | "AVOID" | null;
  reasons: string[];
};
