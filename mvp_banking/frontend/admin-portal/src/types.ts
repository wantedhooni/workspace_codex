export type ApiEnvelope<T> = {
  success: boolean;
  data: T;
};

export type PageResponse<T> = {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresIn: number;
  refreshTokenExpiresIn: number;
  principalType: "ADMIN" | "USER";
  email: string;
  displayName: string;
};

export type Profile = {
  id: string;
  email: string;
  displayName: string;
  principalType: "ADMIN" | "USER";
  roles: string[];
};

export type Customer = {
  id: string;
  customerNumber: string;
  fullName: string;
  email: string;
  status: string;
  createdAt: string;
};

export type CustomerStatusSummary = {
  active: number;
  reviewRequired: number;
  suspended: number;
  total: number;
};

export type Account = {
  id: string;
  accountNumber: string;
  accountType: string;
  status: string;
  balance: number;
  currency: string;
  customerId: string;
};

export type Transaction = {
  id: string;
  accountId: string;
  transactionNumber: string;
  transactionType: string;
  status: string;
  amount: number;
  currency: string;
  description: string | null;
  occurredAt: string;
};

export type FxRate = {
  id: string;
  baseCurrency: string;
  quoteCurrency: string;
  rate: number;
  effectiveAt: string;
  source: string;
};

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

export type LinkedBankAccount = {
  id: string;
  customerId: string;
  customerEmail: string;
  bankName: string;
  accountAlias: string;
  accountHolderName: string;
  maskedAccountNumber: string;
  status: string;
  primaryWithdrawal: boolean;
  verifiedAt: string | null;
  verificationReference: string | null;
  verificationRequestedAt: string | null;
  verificationExpiresAt: string | null;
  verificationExpired: boolean;
  lastVerificationResentAt: string | null;
  verificationResendAvailableAt: string | null;
  verificationResendAllowed: boolean;
  verificationAttemptCount: number;
  blockReasonCode: string | null;
  blockedAt: string | null;
  createdAt: string;
};

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

export type Approval = {
  id: string;
  targetType: string;
  targetId: string;
  title: string;
  description: string;
  status: string;
  requestedByEmail: string;
  decisionByEmail: string | null;
  decisionReason: string | null;
  decidedAt: string | null;
};

export type AuditLog = {
  id: string;
  actorType: string;
  actorId: string | null;
  actorEmail: string;
  actionType: string;
  targetType: string;
  targetId: string | null;
  description: string;
  loggedAt: string;
};

export type Notification = {
  id: string;
  category: string;
  severity: string;
  title: string;
  message: string;
  actionPath: string;
  referenceType: string | null;
  referenceId: string | null;
  read: boolean;
  readAt: string | null;
  createdAt: string;
};

export type NotificationList = {
  unreadCount: number;
  items: Notification[];
};

export type Announcement = {
  id: string;
  title: string;
  summary: string;
  body: string;
  severity: string;
  audience: string;
  status: string;
  pinned: boolean;
  startsAt: string | null;
  endsAt: string | null;
  publishedAt: string | null;
  archivedAt: string | null;
  createdByEmail: string;
  createdAt: string;
};

export type CreateAnnouncementPayload = {
  title: string;
  summary: string;
  body: string;
  severity: string;
  audience: string;
  pinned: boolean;
  startsAt?: string | null;
  endsAt?: string | null;
};

export type UpdateAnnouncementPayload = CreateAnnouncementPayload;

export type AdminOverview = {
  metrics: {
    pendingApprovals: number;
    overdueApprovals: number;
    reviewRequiredCustomers: number;
    lockedAccounts: number;
    pendingFundingRequests: number;
    pendingExchanges: number;
    pendingStockOrders: number;
    partiallyFilledOrders: number;
    pendingInstructionVolumeKrw: number;
  };
  marketStatus: {
    latestFxEffectiveAt: string | null;
    latestStockQuoteEffectiveAt: string | null;
    freshFxPairs: number;
    staleFxPairs: number;
    freshStockQuotes: number;
    staleStockQuotes: number;
  };
  alerts: Array<{
    severity: string;
    title: string;
    message: string;
    actionPath: string;
  }>;
};

export type AdminListParams = {
  query?: string;
  status?: string;
  page?: number;
  size?: number;
  accountType?: string;
  transactionType?: string;
  sortBy?: string;
  sortDir?: string;
  minBalance?: number | string;
  maxBalance?: number | string;
  minAmount?: number | string;
  maxAmount?: number | string;
  occurredFrom?: string;
  occurredTo?: string;
  createdFrom?: string;
  createdTo?: string;
};
