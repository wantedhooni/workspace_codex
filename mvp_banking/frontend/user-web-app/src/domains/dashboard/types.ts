export type DashboardInsight = {
  totalAssetsKrw: number;
  cashAssetsKrw: number;
  investmentAssetsKrw: number;
  unrealizedProfitLossKrw: number;
  realizedProfitLossKrw: number;
  activeAccountCount: number;
  pendingInstructionCount: number;
  partiallyFilledOrderCount: number;
  attentionItems: Array<{
    severity: string;
    title: string;
    message: string;
    actionPath: string;
  }>;
  currencyExposures: Array<{
    currency: string;
    cashBalance: number;
    positionValue: number;
    totalExposure: number;
    krwEquivalent: number;
  }>;
  topPositions: Array<{
    symbol: string;
    market: string;
    marketValue: number;
    unrealizedProfitLoss: number;
    unrealizedProfitRate: number;
    currency: string;
  }>;
};
