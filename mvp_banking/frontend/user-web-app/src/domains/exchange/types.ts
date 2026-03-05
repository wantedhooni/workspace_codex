export type ExchangeRequest = {
  id: string;
  customerId: string;
  sourceAccountId: string | null;
  destinationAccountId: string;
  requestNumber: string;
  fromCurrency: string;
  toCurrency: string;
  fromAmount: number;
  appliedRate: number;
  toAmount: number;
  exchangeFeeAmount: number;
  netToAmount: number;
  appliedRateEffectiveAt: string;
  requestMemo: string | null;
  sameDaySettlementEligible: boolean;
  expectedSettlementAt: string | null;
  manualReviewRequired: boolean;
  manualReviewReason: string | null;
  cancellationReason: string | null;
  canceledAt: string | null;
  status: string;
  sourceTransactionNumber: string | null;
  destinationTransactionNumber: string | null;
  settledAt: string | null;
  createdAt: string;
};

export type CreateExchangeRequestPayload = {
  sourceAccountId: string;
  destinationAccountId: string;
  fromAmount: number;
  requestMemo?: string;
};

export type CancelExchangeRequestPayload = {
  reason?: string;
};
