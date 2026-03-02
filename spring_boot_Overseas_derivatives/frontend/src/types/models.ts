export type Role = "OPS_ADMIN" | "OPS_VIEWER" | "AUDITOR";

export type Account = {
  id: number;
  accountNo: string;
  broker: string;
  status: string;
  ownerName: string;
  openedAt: string;
  closedAt?: string;
};

export type PagedResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};

export type RequestRow = {
  id: string;
  requestType: string;
  status: string;
  accountId: number;
  accountNo: string;
  amount: number;
  currency: string;
  fromCurrency?: string;
  toCurrency?: string;
  exchangeRate?: number;
  expectedToAmount?: number;
  exchangeRateDate?: string;
  exchangeRateSource?: string;
  priority?: "LOW" | "NORMAL" | "HIGH" | "URGENT";
  valueDate?: string;
  manualReviewRequired?: boolean;
  controlReason?: string;
  controlPolicyId?: number;
  controlPolicySource?: string;
  controlLimitPolicyId?: number;
  controlLimitPolicySource?: string;
  projectedDailyExposure?: number;
  slaDueAt?: string;
  requestedBy: string;
  reviewedBy?: string;
  reason: string;
  reviewReason?: string;
  requestedAt: string;
  reviewedAt?: string;
};

export type BatchRun = {
  id: number;
  batchName: string;
  status: string;
  startedAt: string;
  finishedAt?: string;
  errorMessage?: string;
  retryCount: number;
};

export type BatchSchedule = {
  triggerName: string;
  jobName: string;
  batchName: string;
  cronExpression?: string;
  previousFireTime?: string;
  nextFireTime?: string;
  triggerState: string;
};

export type StockPurchase = {
  id: number;
  accountId: number;
  accountNo: string;
  symbol: string;
  market: string;
  currency: string;
  tradeDate: string;
  settlementDate: string;
  quantity: number;
  price: number;
  grossAmount: number;
  feeAmount: number;
  netAmount: number;
  brokerOrderNo: string;
  createdBy: string;
  createdAt: string;
};

export type StockPosition = {
  id: number;
  accountId: number;
  accountNo: string;
  symbol: string;
  market: string;
  currency: string;
  quantity: number;
  averagePrice: number;
  totalCost: number;
  lastTradeDate: string;
  updatedAt: string;
};

export type PortfolioOverview = {
  accountId: number;
  accountNo: string;
  broker: string;
  status: string;
  ownerName: string;
  cashSnapshotDate: string;
  holdingCount: number;
  recentPurchaseCount: number;
  cashBalances: Array<{
    currency: string;
    amount: number;
  }>;
  stockCostByCurrency: Array<{
    currency: string;
    totalCost: number;
  }>;
  holdings: Array<{
    symbol: string;
    market: string;
    currency: string;
    quantity: number;
    averagePrice: number;
    totalCost: number;
    lastTradeDate: string;
  }>;
  recentPurchases: Array<{
    id: number;
    symbol: string;
    market: string;
    currency: string;
    tradeDate: string;
    settlementDate: string;
    quantity: number;
    price: number;
    feeAmount: number;
    netAmount: number;
  }>;
};

export type DomainTerm = {
  id: number;
  domainKey: string;
  domainName: string;
  termKey: string;
  termName: string;
  koreanName: string;
  description: string;
  exampleText?: string;
  domainSortOrder: number;
  sortOrder: number;
};

export type ExchangeRate = {
  id: number;
  fromCurrency: string;
  toCurrency: string;
  rateDate: string;
  rate: number;
  source: string;
  createdAt: string;
};

export type ExchangeRateQuote = {
  fromCurrency: string;
  toCurrency: string;
  requestedAmount: number;
  exchangeRate: number;
  convertedAmount: number;
  rateDate: string;
  source: string;
  quoteMode: string;
};

export type RecommendationRiskProfile = "CONSERVATIVE" | "BALANCED" | "AGGRESSIVE";
export type RecommendationHorizon = "SHORT_TERM" | "MEDIUM_TERM" | "LONG_TERM";
export type RecommendationAction = "BUY" | "WATCH" | "HOLD";
export type RecommendationConfidence = "LOW" | "MEDIUM" | "HIGH";

export type StockRecommendationResult = {
  accountId: number;
  accountNo: string;
  provider: string;
  model: string;
  generatedAt: string;
  summary: string;
  cautionPoints: string[];
  recommendations: Array<{
    rank: number;
    symbol: string;
    market: string;
    action: RecommendationAction;
    confidence: RecommendationConfidence;
    allocationHint: string;
    rationale: string;
    riskNotes: string;
  }>;
  portfolioSnapshot: {
    cashSnapshotDate: string;
    cashBalances: Array<{
      currency: string;
      amount: number;
    }>;
    holdings: Array<{
      symbol: string;
      market: string;
      currency: string;
      quantity: number;
      averagePrice: number;
      totalCost: number;
      lastTradeDate: string;
    }>;
  };
  disclaimer: string;
};

export type MenuItem = {
  id: number;
  menuKey: string;
  title: string;
  description?: string;
  path: string;
  parentMenuKey?: string;
  depth: number;
  resourceName?: string;
  icon?: string;
  sortOrder: number;
  enabled: boolean;
  roles: string[];
  children: MenuItem[];
};

export type AuditLog = {
  id: number;
  actor: string;
  action: string;
  targetType: string;
  targetId?: string;
  details?: string;
  createdAt: string;
};

export type ApprovalDomain = "CASH_DEPOSIT" | "CASH_WITHDRAW" | "FX";

export type ApprovalPolicy = {
  id: number;
  brokerCode: string;
  domain: ApprovalDomain;
  highThreshold: number;
  urgentThreshold: number;
  manualReviewThreshold: number;
  sameDayAutoReview: boolean;
  enabled: boolean;
  effectiveFrom: string;
  effectiveTo?: string;
  description?: string;
  createdAt: string;
};

export type RiskLimitPolicy = {
  id: number;
  brokerCode: string;
  domain: ApprovalDomain;
  currencyCode?: string;
  maxPerRequest: number;
  dailySoftLimit: number;
  dailyHardLimit: number;
  enabled: boolean;
  effectiveFrom: string;
  effectiveTo?: string;
  description?: string;
  createdAt: string;
};

export type OpsCaseStatus = "OPEN" | "IN_PROGRESS" | "RESOLVED" | "CLOSED";
export type OpsCaseSeverity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
export type OpsCaseCategory = "REQUEST_FAILURE" | "BATCH_FAILURE" | "INTEGRATION" | "CONTROL" | "DATA_QUALITY" | "OTHER";

export type OpsCase = {
  id: number;
  caseNo: string;
  category: OpsCaseCategory;
  severity: OpsCaseSeverity;
  status: OpsCaseStatus;
  title: string;
  description: string;
  assignee?: string;
  dueAt?: string;
  linkedType?: string;
  linkedId?: string;
  accountId?: number;
  resolutionSummary?: string;
  createdBy: string;
  updatedBy?: string;
  resolvedBy?: string;
  resolvedAt?: string;
  closedBy?: string;
  closedAt?: string;
  createdAt: string;
  updatedAt: string;
};
