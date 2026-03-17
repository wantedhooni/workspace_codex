export type NewsAnalysis = {
  id: number;
  headline: string;
  sentiment: "POSITIVE" | "NEGATIVE" | "NEUTRAL";
  summary: string;
  impact: string;
  interpretation: string;
  analyzedAt: string;
};
