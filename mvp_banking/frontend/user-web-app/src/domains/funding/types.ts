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
  serviceFeeAmount: number;
  priorityProcessing: boolean;
  priorityFeeAmount: number;
  totalDebitAmount: number;
  currency: string;
  balanceSnapshot: number;
  linkedBankAccountId: string | null;
  linkedBankName: string | null;
  linkedBankAccountAlias: string | null;
  linkedBankAccountNumberMasked: string | null;
  linkedBankAccountHolderName: string | null;
  dailyLimitAmount: number | null;
  dailyAccumulatedAmount: number | null;
  dailyLimitExceeded: boolean;
  sameDaySettlementEligible: boolean;
  expectedSettlementAt: string | null;
  manualReviewRequired: boolean;
  manualReviewReason: string | null;
  note: string | null;
  cancellationReason: string | null;
  canceledAt: string | null;
  settlementTransactionNumber: string | null;
  settledAt: string | null;
  createdAt: string;
};

export type CreateFundingRequestPayload = {
  accountId: string;
  requestType: string;
  amount: number;
  linkedBankAccountId?: string;
  priorityProcessing?: boolean;
  note?: string;
};

export type CancelFundingRequestPayload = {
  reason?: string;
};
