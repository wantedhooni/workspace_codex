import type { DashboardInsight } from "../../domains/dashboard/types";
import type { Account } from "../../domains/accounts/types";
import type { Announcement } from "../../domains/announcements/types";
import type { Profile } from "../../domains/auth/types";
import type { ExchangeRequest } from "../../domains/exchange/types";
import type { FxRate } from "../../domains/fx/types";
import type { FundingRequest } from "../../domains/funding/types";
import type { Notification } from "../../domains/notifications/types";
import type { StockOrder, StockPosition } from "../../domains/stock/types";
import type { Transaction } from "../../domains/transactions/types";

export type DashboardState = {
  profile: Profile;
  insights: DashboardInsight;
  announcements: Announcement[];
  accounts: Account[];
  fundingRequests: FundingRequest[];
  transactions: Transaction[];
  fxRates: FxRate[];
  exchangeRequests: ExchangeRequest[];
  stockOrders: StockOrder[];
  stockPositions: StockPosition[];
  notifications: Notification[];
  unreadNotificationCount: number;
};
