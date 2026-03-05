export type StockOrderExecution = {
  id: string;
  executionNumber: string;
  executionSequence: number;
  executedQuantity: number;
  executedPrice: number;
  executedAmount: number;
  executedAt: string;
};

export type StockOrder = {
  id: string;
  customerId: string;
  accountId: string;
  orderNumber: string;
  symbol: string;
  market: string;
  side: string;
  quantity: number;
  limitPrice: number;
  grossAmount: number;
  currency: string;
  status: string;
  executedQuantity: number | null;
  executedPrice: number | null;
  remainingQuantity: number;
  fillRate: number;
  feeAmount: number;
  taxAmount: number;
  netSettlementAmount: number;
  orderMemo: string | null;
  timeInForce: string;
  expiresAt: string;
  cancellationReason: string | null;
  canceledAt: string | null;
  marketSession: string;
  expectedExecutionAt: string;
  manualReviewRequired: boolean;
  manualReviewReason: string | null;
  referencePrice: number | null;
  priceDeviationRate: number | null;
  quoteEffectiveAt: string | null;
  quoteSource: string | null;
  settlementTransactionNumber: string | null;
  executions: StockOrderExecution[];
  settledAt: string | null;
  createdAt: string;
};

export type StockPosition = {
  id: string;
  customerId: string;
  accountId: string;
  symbol: string;
  market: string;
  quantity: number;
  averagePrice: number;
  currency: string;
  costBasis: number;
  currentPrice: number | null;
  marketValue: number | null;
  unrealizedProfitLoss: number | null;
  unrealizedProfitRate: number | null;
  realizedProfitLoss: number;
  changeRate: number | null;
  quoteEffectiveAt: string | null;
  quoteSource: string | null;
  updatedAt: string;
};

export type CreateStockOrderPayload = {
  accountId: string;
  symbol: string;
  market: string;
  side: string;
  quantity: number;
  limitPrice: number;
  currency: string;
  timeInForce?: string;
  orderMemo?: string;
};

export type CancelStockOrderPayload = {
  reason?: string;
};
