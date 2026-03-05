import { adminAccountsApi } from "./domains/accounts/api";
import { adminAnnouncementsApi } from "./domains/announcements/api";
import { adminApprovalsApi } from "./domains/approvals/api";
import { adminAuditApi } from "./domains/audit/api";
import { adminAuthApi } from "./domains/auth/api";
import { adminCustomersApi } from "./domains/customers/api";
import { adminExchangeApi } from "./domains/exchange/api";
import { adminFundingApi } from "./domains/funding/api";
import { adminFxApi } from "./domains/fx/api";
import { adminLinkedBankAccountsApi } from "./domains/linked-bank-accounts/api";
import { adminNotificationsApi } from "./domains/notifications/api";
import { adminOverviewApi } from "./domains/overview/api";
import { adminStockApi } from "./domains/stock/api";
import { adminTransactionsApi } from "./domains/transactions/api";

export * from "./types";

export const adminApi = {
  ...adminAuthApi,
  ...adminOverviewApi,
  ...adminCustomersApi,
  ...adminAccountsApi,
  ...adminTransactionsApi,
  ...adminFxApi,
  ...adminFundingApi,
  ...adminLinkedBankAccountsApi,
  ...adminExchangeApi,
  ...adminStockApi,
  ...adminApprovalsApi,
  ...adminAuditApi,
  ...adminNotificationsApi,
  ...adminAnnouncementsApi,
};
