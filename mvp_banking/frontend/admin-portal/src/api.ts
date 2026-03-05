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

type AdminListParams = {
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

async function request<T>(path: string, options: RequestInit = {}, token?: string): Promise<T> {
  const response = await fetch(path, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers ?? {}),
    },
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed: ${response.status}`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const json = (await response.json()) as ApiEnvelope<T>;
  return json.data;
}

function withQuery(path: string, params: AdminListParams) {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      searchParams.set(key, String(value));
    }
  });

  const query = searchParams.toString();
  return query ? `${path}?${query}` : path;
}

export const adminApi = {
  login(email: string, password: string) {
    return request<AuthResponse>("/api/admin/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
  },
  me(token: string) {
    return request<Profile>("/api/admin/me", {}, token);
  },
  overview(token: string) {
    return request<AdminOverview>("/api/admin/overview", {}, token);
  },
  customers(token: string, params: AdminListParams) {
    return request<PageResponse<Customer>>(withQuery("/api/admin/customers", params), {}, token);
  },
  customerSummary(token: string, params: AdminListParams) {
    return request<CustomerStatusSummary>(withQuery("/api/admin/customers/summary", params), {}, token);
  },
  accounts(token: string, params: AdminListParams) {
    return request<PageResponse<Account>>(withQuery("/api/admin/accounts", params), {}, token);
  },
  transactions(token: string, params: AdminListParams) {
    return request<PageResponse<Transaction>>(withQuery("/api/admin/transactions", params), {}, token);
  },
  fxRates(token: string) {
    return request<FxRate[]>("/api/admin/fx-rates", {}, token);
  },
  fundingRequests(token: string) {
    return request<FundingRequest[]>("/api/admin/funding-requests", {}, token);
  },
  linkedBankAccounts(token: string) {
    return request<LinkedBankAccount[]>("/api/admin/linked-bank-accounts", {}, token);
  },
  activateLinkedBankAccount(token: string, linkedBankAccountId: string) {
    return request<LinkedBankAccount>(
      `/api/admin/linked-bank-accounts/${linkedBankAccountId}/activate`,
      {
        method: "POST",
      },
      token,
    );
  },
  blockLinkedBankAccount(token: string, linkedBankAccountId: string) {
    return request<LinkedBankAccount>(
      `/api/admin/linked-bank-accounts/${linkedBankAccountId}/block`,
      {
        method: "POST",
      },
      token,
    );
  },
  exchangeRequests(token: string) {
    return request<ExchangeRequest[]>("/api/admin/exchange-requests", {}, token);
  },
  stockOrders(token: string) {
    return request<StockOrder[]>("/api/admin/stock-orders", {}, token);
  },
  completeStockOrderFill(token: string, orderId: string) {
    return request<void>(
      `/api/admin/stock-orders/${orderId}/complete-fill`,
      {
        method: "POST",
      },
      token,
    );
  },
  stockPositions(token: string) {
    return request<StockPosition[]>("/api/admin/stock-positions", {}, token);
  },
  approvals(token: string) {
    return request<Approval[]>("/api/admin/approvals", {}, token);
  },
  approve(token: string, approvalId: string, reason: string) {
    return request<Approval>(
      `/api/admin/approvals/${approvalId}/approve`,
      {
        method: "POST",
        body: JSON.stringify({ reason }),
      },
      token,
    );
  },
  reject(token: string, approvalId: string, reason: string) {
    return request<Approval>(
      `/api/admin/approvals/${approvalId}/reject`,
      {
        method: "POST",
        body: JSON.stringify({ reason }),
      },
      token,
    );
  },
  auditLogs(token: string) {
    return request<AuditLog[]>("/api/admin/audit-logs", {}, token);
  },
  notifications(token: string) {
    return request<NotificationList>("/api/admin/notifications", {}, token);
  },
  announcements(token: string) {
    return request<Announcement[]>("/api/admin/announcements", {}, token);
  },
  createAnnouncement(token: string, payload: CreateAnnouncementPayload) {
    return request<Announcement>(
      "/api/admin/announcements",
      {
        method: "POST",
        body: JSON.stringify(payload),
      },
      token,
    );
  },
  publishAnnouncement(token: string, announcementId: string) {
    return request<Announcement>(
      `/api/admin/announcements/${announcementId}/publish`,
      {
        method: "POST",
      },
      token,
    );
  },
  archiveAnnouncement(token: string, announcementId: string) {
    return request<Announcement>(
      `/api/admin/announcements/${announcementId}/archive`,
      {
        method: "POST",
      },
      token,
    );
  },
  markNotificationRead(token: string, notificationId: string) {
    return request<Notification>(
      `/api/admin/notifications/${notificationId}/read`,
      {
        method: "POST",
      },
      token,
    );
  },
};
