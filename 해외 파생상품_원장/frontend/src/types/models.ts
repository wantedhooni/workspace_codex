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
  amount: number;
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

export type AuditLog = {
  id: number;
  actor: string;
  action: string;
  targetType: string;
  targetId?: string;
  details?: string;
  createdAt: string;
};
