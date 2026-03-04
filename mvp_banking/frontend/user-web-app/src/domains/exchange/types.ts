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
};
