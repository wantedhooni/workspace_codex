export type FundingRequest = {
  id: string;
  customerId: string;
  customerEmail: string;
  accountId: string;
  accountNumber: string;
  accountType: string;
  requestNumber: string;
  requestType: string;
  status: string;
  amount: number;
  currency: string;
  balanceSnapshot: number;
  note: string | null;
  settlementTransactionNumber: string | null;
  settledAt: string | null;
  createdAt: string;
};

export type CreateFundingRequestPayload = {
  accountId: string;
  requestType: string;
  amount: number;
  note?: string;
};
