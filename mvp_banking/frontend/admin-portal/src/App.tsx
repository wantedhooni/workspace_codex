import { Suspense, lazy, startTransition, useEffect, useState } from "react";
import { Refine } from "@refinedev/core";
import { App as AntdApp, ConfigProvider, Spin, message } from "antd";
import { Navigate, Route, Routes, useLocation, useNavigate } from "react-router-dom";
import {
  adminApi,
  type Account,
  type AdminOverview,
  type Announcement,
  type Approval,
  type AuditLog,
  type Customer,
  type CustomerStatusSummary,
  type ExchangeRequest,
  type FxRate,
  type FundingRequest,
  type LinkedBankAccount,
  type Notification,
  type PageResponse,
  type Profile,
  type StockOrder,
  type StockPosition,
  type Transaction,
} from "./api";
const AdminShell = lazy(() => import("./components/AdminShell").then((module) => ({ default: module.AdminShell })));
const LoginScreen = lazy(() => import("./components/LoginScreen").then((module) => ({ default: module.LoginScreen })));
const OverviewPage = lazy(() => import("./pages/OverviewPage").then((module) => ({ default: module.OverviewPage })));
const CustomersPage = lazy(() => import("./pages/CustomersPage").then((module) => ({ default: module.CustomersPage })));
const AccountsPage = lazy(() => import("./pages/AccountsPage").then((module) => ({ default: module.AccountsPage })));
const TransactionsPage = lazy(() => import("./pages/TransactionsPage").then((module) => ({ default: module.TransactionsPage })));
const FxRatesPage = lazy(() => import("./pages/FxRatesPage").then((module) => ({ default: module.FxRatesPage })));
const LinkedBankAccountsPage = lazy(() => import("./pages/LinkedBankAccountsPage").then((module) => ({ default: module.LinkedBankAccountsPage })));
const FundingRequestsPage = lazy(() => import("./pages/FundingRequestsPage").then((module) => ({ default: module.FundingRequestsPage })));
const ExchangeRequestsPage = lazy(() => import("./pages/ExchangeRequestsPage").then((module) => ({ default: module.ExchangeRequestsPage })));
const StockOrdersPage = lazy(() => import("./pages/StockOrdersPage").then((module) => ({ default: module.StockOrdersPage })));
const StockPositionsPage = lazy(() => import("./pages/StockPositionsPage").then((module) => ({ default: module.StockPositionsPage })));
const ApprovalsPage = lazy(() => import("./pages/ApprovalsPage").then((module) => ({ default: module.ApprovalsPage })));
const AnnouncementsPage = lazy(() => import("./pages/AnnouncementsPage").then((module) => ({ default: module.AnnouncementsPage })));
const NotificationsPage = lazy(() => import("./pages/NotificationsPage").then((module) => ({ default: module.NotificationsPage })));
const AuditLogsPage = lazy(() => import("./pages/AuditLogsPage").then((module) => ({ default: module.AuditLogsPage })));
import "./styles.css";

type CustomerFilter = {
  query: string;
  status?: string;
  createdFrom?: string;
  createdTo?: string;
  sortBy: string;
  sortDir: string;
  page: number;
  size: number;
};

type AccountFilter = {
  query: string;
  status?: string;
  accountType?: string;
  minBalance?: string;
  maxBalance?: string;
  sortBy: string;
  sortDir: string;
  page: number;
  size: number;
};

type TransactionFilter = {
  query: string;
  status?: string;
  transactionType?: string;
  minAmount?: string;
  maxAmount?: string;
  occurredFrom?: string;
  occurredTo?: string;
  sortBy: string;
  sortDir: string;
  page: number;
  size: number;
};

const ADMIN_TOKEN_KEY = "mvp-banking-admin-token";
const DEFAULT_CUSTOMER_FILTER: CustomerFilter = {
  query: "",
  sortBy: "createdAt",
  sortDir: "desc",
  page: 0,
  size: 5,
};
const DEFAULT_ACCOUNT_FILTER: AccountFilter = {
  query: "",
  sortBy: "createdAt",
  sortDir: "desc",
  page: 0,
  size: 5,
};
const DEFAULT_TRANSACTION_FILTER: TransactionFilter = {
  query: "",
  sortBy: "occurredAt",
  sortDir: "desc",
  page: 0,
  size: 5,
};

function parseOptionalParam(params: URLSearchParams, key: string): string | undefined {
  const value = params.get(key);
  if (!value) {
    return undefined;
  }
  const trimmed = value.trim();
  return trimmed.length ? trimmed : undefined;
}

function parsePositiveInt(value: string | undefined, fallback: number): number {
  if (!value) {
    return fallback;
  }
  const parsed = Number(value);
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return fallback;
  }
  return Math.floor(parsed);
}

function buildCustomerSearch(filter: CustomerFilter) {
  const params = new URLSearchParams();
  if (filter.query.trim()) {
    params.set("query", filter.query.trim());
  }
  if (filter.status) {
    params.set("status", filter.status);
  }
  if (filter.createdFrom) {
    params.set("createdFrom", filter.createdFrom);
  }
  if (filter.createdTo) {
    params.set("createdTo", filter.createdTo);
  }
  params.set("sortBy", filter.sortBy);
  params.set("sortDir", filter.sortDir);
  params.set("page", String(filter.page + 1));
  params.set("size", String(filter.size));
  return params.toString();
}

function buildAccountSearch(filter: AccountFilter) {
  const params = new URLSearchParams();
  if (filter.query.trim()) {
    params.set("query", filter.query.trim());
  }
  if (filter.status) {
    params.set("status", filter.status);
  }
  if (filter.accountType) {
    params.set("accountType", filter.accountType);
  }
  if (filter.minBalance) {
    params.set("minBalance", filter.minBalance);
  }
  if (filter.maxBalance) {
    params.set("maxBalance", filter.maxBalance);
  }
  params.set("sortBy", filter.sortBy);
  params.set("sortDir", filter.sortDir);
  params.set("page", String(filter.page + 1));
  params.set("size", String(filter.size));
  return params.toString();
}

function buildTransactionSearch(filter: TransactionFilter) {
  const params = new URLSearchParams();
  if (filter.query.trim()) {
    params.set("query", filter.query.trim());
  }
  if (filter.status) {
    params.set("status", filter.status);
  }
  if (filter.transactionType) {
    params.set("transactionType", filter.transactionType);
  }
  if (filter.minAmount) {
    params.set("minAmount", filter.minAmount);
  }
  if (filter.maxAmount) {
    params.set("maxAmount", filter.maxAmount);
  }
  if (filter.occurredFrom) {
    params.set("occurredFrom", filter.occurredFrom);
  }
  if (filter.occurredTo) {
    params.set("occurredTo", filter.occurredTo);
  }
  params.set("sortBy", filter.sortBy);
  params.set("sortDir", filter.sortDir);
  params.set("page", String(filter.page + 1));
  params.set("size", String(filter.size));
  return params.toString();
}

function parseCustomerFilter(search: string): CustomerFilter {
  const params = new URLSearchParams(search);
  const page = parsePositiveInt(parseOptionalParam(params, "page"), DEFAULT_CUSTOMER_FILTER.page + 1) - 1;
  const size = parsePositiveInt(parseOptionalParam(params, "size"), DEFAULT_CUSTOMER_FILTER.size);
  return {
    query: parseOptionalParam(params, "query") ?? DEFAULT_CUSTOMER_FILTER.query,
    status: parseOptionalParam(params, "status"),
    createdFrom: parseOptionalParam(params, "createdFrom"),
    createdTo: parseOptionalParam(params, "createdTo"),
    sortBy: parseOptionalParam(params, "sortBy") ?? DEFAULT_CUSTOMER_FILTER.sortBy,
    sortDir: parseOptionalParam(params, "sortDir") ?? DEFAULT_CUSTOMER_FILTER.sortDir,
    page,
    size,
  };
}

function parseAccountFilter(search: string): AccountFilter {
  const params = new URLSearchParams(search);
  const page = parsePositiveInt(parseOptionalParam(params, "page"), DEFAULT_ACCOUNT_FILTER.page + 1) - 1;
  const size = parsePositiveInt(parseOptionalParam(params, "size"), DEFAULT_ACCOUNT_FILTER.size);
  return {
    query: parseOptionalParam(params, "query") ?? DEFAULT_ACCOUNT_FILTER.query,
    status: parseOptionalParam(params, "status"),
    accountType: parseOptionalParam(params, "accountType"),
    minBalance: parseOptionalParam(params, "minBalance"),
    maxBalance: parseOptionalParam(params, "maxBalance"),
    sortBy: parseOptionalParam(params, "sortBy") ?? DEFAULT_ACCOUNT_FILTER.sortBy,
    sortDir: parseOptionalParam(params, "sortDir") ?? DEFAULT_ACCOUNT_FILTER.sortDir,
    page,
    size,
  };
}

function parseTransactionFilter(search: string): TransactionFilter {
  const params = new URLSearchParams(search);
  const page = parsePositiveInt(parseOptionalParam(params, "page"), DEFAULT_TRANSACTION_FILTER.page + 1) - 1;
  const size = parsePositiveInt(parseOptionalParam(params, "size"), DEFAULT_TRANSACTION_FILTER.size);
  return {
    query: parseOptionalParam(params, "query") ?? DEFAULT_TRANSACTION_FILTER.query,
    status: parseOptionalParam(params, "status"),
    transactionType: parseOptionalParam(params, "transactionType"),
    minAmount: parseOptionalParam(params, "minAmount"),
    maxAmount: parseOptionalParam(params, "maxAmount"),
    occurredFrom: parseOptionalParam(params, "occurredFrom"),
    occurredTo: parseOptionalParam(params, "occurredTo"),
    sortBy: parseOptionalParam(params, "sortBy") ?? DEFAULT_TRANSACTION_FILTER.sortBy,
    sortDir: parseOptionalParam(params, "sortDir") ?? DEFAULT_TRANSACTION_FILTER.sortDir,
    page,
    size,
  };
}

function App() {
  const location = useLocation();
  const navigate = useNavigate();
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(ADMIN_TOKEN_KEY));
  const [submitting, setSubmitting] = useState(false);
  const [shellLoading, setShellLoading] = useState(false);
  const [approvalSubmitting, setApprovalSubmitting] = useState(false);
  const [stockFillSubmittingId, setStockFillSubmittingId] = useState<string | null>(null);
  const [activatingLinkedBankAccountId, setActivatingLinkedBankAccountId] = useState<string | null>(null);
  const [blockingLinkedBankAccountId, setBlockingLinkedBankAccountId] = useState<string | null>(null);
  const [profile, setProfile] = useState<Profile | null>(null);
  const [overview, setOverview] = useState<AdminOverview | null>(null);
  const [announcements, setAnnouncements] = useState<Announcement[]>([]);
  const [approvals, setApprovals] = useState<Approval[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadNotificationCount, setUnreadNotificationCount] = useState(0);
  const [fxRates, setFxRates] = useState<FxRate[]>([]);
  const [linkedBankAccounts, setLinkedBankAccounts] = useState<LinkedBankAccount[]>([]);
  const [fundingRequests, setFundingRequests] = useState<FundingRequest[]>([]);
  const [exchangeRequests, setExchangeRequests] = useState<ExchangeRequest[]>([]);
  const [stockOrders, setStockOrders] = useState<StockOrder[]>([]);
  const [stockPositions, setStockPositions] = useState<StockPosition[]>([]);
  const [customerSummary, setCustomerSummary] = useState<CustomerStatusSummary | null>(null);
  const [customersPage, setCustomersPage] = useState<PageResponse<Customer> | null>(null);
  const [accountsPage, setAccountsPage] = useState<PageResponse<Account> | null>(null);
  const [transactionsPage, setTransactionsPage] = useState<PageResponse<Transaction> | null>(null);
  const initialCustomerFilter = location.pathname === "/customers" ? parseCustomerFilter(location.search) : DEFAULT_CUSTOMER_FILTER;
  const initialAccountFilter = location.pathname === "/accounts" ? parseAccountFilter(location.search) : DEFAULT_ACCOUNT_FILTER;
  const initialTransactionFilter = location.pathname === "/transactions" ? parseTransactionFilter(location.search) : DEFAULT_TRANSACTION_FILTER;
  const [customerFilter, setCustomerFilter] = useState<CustomerFilter>(initialCustomerFilter);
  const [accountFilter, setAccountFilter] = useState<AccountFilter>(initialAccountFilter);
  const [transactionFilter, setTransactionFilter] = useState<TransactionFilter>(initialTransactionFilter);
  const [customerSearchFilter, setCustomerSearchFilter] = useState<CustomerFilter>(initialCustomerFilter);
  const [accountSearchFilter, setAccountSearchFilter] = useState<AccountFilter>(initialAccountFilter);
  const [transactionSearchFilter, setTransactionSearchFilter] = useState<TransactionFilter>(initialTransactionFilter);
  const [approvalModal, setApprovalModal] = useState<{ id: string; action: "approve" | "reject" } | null>(null);
  const [approvalReason, setApprovalReason] = useState("");
  const [readingNotificationId, setReadingNotificationId] = useState<string | null>(null);
  const [creatingAnnouncement, setCreatingAnnouncement] = useState(false);
  const [actingAnnouncementId, setActingAnnouncementId] = useState<string | null>(null);
  const [messageApi, contextHolder] = message.useMessage();

  async function loadShell(currentToken: string) {
    setShellLoading(true);
    try {
      const [
        loadedProfile,
        loadedOverview,
        loadedAnnouncements,
        loadedApprovals,
        loadedAuditLogs,
        loadedNotifications,
        loadedFxRates,
        loadedLinkedBankAccounts,
        loadedFundingRequests,
        loadedExchangeRequests,
        loadedStockOrders,
        loadedStockPositions,
      ] = await Promise.all([
        adminApi.me(currentToken),
        adminApi.overview(currentToken),
        adminApi.announcements(currentToken),
        adminApi.approvals(currentToken),
        adminApi.auditLogs(currentToken),
        adminApi.notifications(currentToken),
        adminApi.fxRates(currentToken),
        adminApi.linkedBankAccounts(currentToken),
        adminApi.fundingRequests(currentToken),
        adminApi.exchangeRequests(currentToken),
        adminApi.stockOrders(currentToken),
        adminApi.stockPositions(currentToken),
      ]);

      startTransition(() => {
        setProfile(loadedProfile);
        setOverview(loadedOverview);
        setAnnouncements(loadedAnnouncements);
        setApprovals(loadedApprovals);
        setAuditLogs(loadedAuditLogs);
        setNotifications(loadedNotifications.items);
        setUnreadNotificationCount(loadedNotifications.unreadCount);
        setFxRates(loadedFxRates);
        setLinkedBankAccounts(loadedLinkedBankAccounts);
        setFundingRequests(loadedFundingRequests);
        setExchangeRequests(loadedExchangeRequests);
        setStockOrders(loadedStockOrders);
        setStockPositions(loadedStockPositions);
      });
    } catch (error) {
      localStorage.removeItem(ADMIN_TOKEN_KEY);
      setToken(null);
      setProfile(null);
      setOverview(null);
      setAnnouncements([]);
      setApprovals([]);
      setAuditLogs([]);
      setNotifications([]);
      setUnreadNotificationCount(0);
      setFxRates([]);
      setLinkedBankAccounts([]);
      setFundingRequests([]);
      setExchangeRequests([]);
      setStockOrders([]);
      setStockPositions([]);
      messageApi.error(error instanceof Error ? error.message : "Admin session expired");
    } finally {
      setShellLoading(false);
    }
  }

  async function handleCreateAnnouncement(payload: {
    title: string;
    summary: string;
    body: string;
    severity: string;
    audience: string;
    pinned: boolean;
    startsAt?: string | null;
    endsAt?: string | null;
  }) {
    if (!token) {
      return;
    }

    setCreatingAnnouncement(true);
    try {
      await adminApi.createAnnouncement(token, payload);
      await loadShell(token);
      messageApi.success("공지 초안을 생성했습니다.");
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : "Announcement creation failed");
    } finally {
      setCreatingAnnouncement(false);
    }
  }

  async function handleUpdateAnnouncement(
    announcementId: string,
    payload: {
      title: string;
      summary: string;
      body: string;
      severity: string;
      audience: string;
      pinned: boolean;
      startsAt?: string | null;
      endsAt?: string | null;
    },
  ) {
    if (!token) {
      return;
    }

    setActingAnnouncementId(announcementId);
    try {
      await adminApi.updateAnnouncement(token, announcementId, payload);
      await loadShell(token);
      messageApi.success("공지 초안을 수정했습니다.");
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : "Announcement update failed");
    } finally {
      setActingAnnouncementId(null);
    }
  }

  async function handlePublishAnnouncement(announcementId: string) {
    if (!token) {
      return;
    }

    setActingAnnouncementId(announcementId);
    try {
      await adminApi.publishAnnouncement(token, announcementId);
      await loadShell(token);
      messageApi.success("공지를 게시했습니다.");
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : "Announcement publish failed");
    } finally {
      setActingAnnouncementId(null);
    }
  }

  async function handleArchiveAnnouncement(announcementId: string) {
    if (!token) {
      return;
    }

    setActingAnnouncementId(announcementId);
    try {
      await adminApi.archiveAnnouncement(token, announcementId);
      await loadShell(token);
      messageApi.success("공지를 종료했습니다.");
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : "Announcement archive failed");
    } finally {
      setActingAnnouncementId(null);
    }
  }

  async function handleMarkNotificationRead(notificationId: string) {
    if (!token) {
      return;
    }

    setReadingNotificationId(notificationId);
    try {
      const updated = await adminApi.markNotificationRead(token, notificationId);
      const shouldDecrement = notifications.some((item) => item.id === notificationId && !item.read);
      startTransition(() => {
        setNotifications((current) =>
          current.map((item) => (item.id === notificationId ? updated : item)),
        );
        if (shouldDecrement) {
          setUnreadNotificationCount((current) => Math.max(0, current - 1));
        }
      });
      messageApi.success("알림을 읽음 처리했습니다.");
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : "Notification update failed");
    } finally {
      setReadingNotificationId(null);
    }
  }

  async function loadCustomers(currentToken: string, nextFilter: CustomerFilter) {
    const response = await adminApi.customers(currentToken, {
      query: nextFilter.query,
      status: nextFilter.status,
      createdFrom: nextFilter.createdFrom,
      createdTo: nextFilter.createdTo,
      sortBy: nextFilter.sortBy,
      sortDir: nextFilter.sortDir,
      page: nextFilter.page,
      size: nextFilter.size,
    });
    startTransition(() => setCustomersPage(response));
  }

  async function loadCustomerSummary(currentToken: string, nextFilter: CustomerFilter) {
    const response = await adminApi.customerSummary(currentToken, {
      query: nextFilter.query,
      createdFrom: nextFilter.createdFrom,
      createdTo: nextFilter.createdTo,
    });
    startTransition(() => setCustomerSummary(response));
  }

  async function loadAccounts(currentToken: string, nextFilter: AccountFilter) {
    const response = await adminApi.accounts(currentToken, {
      query: nextFilter.query,
      status: nextFilter.status,
      accountType: nextFilter.accountType,
      minBalance: nextFilter.minBalance,
      maxBalance: nextFilter.maxBalance,
      sortBy: nextFilter.sortBy,
      sortDir: nextFilter.sortDir,
      page: nextFilter.page,
      size: nextFilter.size,
    });
    startTransition(() => setAccountsPage(response));
  }

  async function loadTransactions(currentToken: string, nextFilter: TransactionFilter) {
    const response = await adminApi.transactions(currentToken, {
      query: nextFilter.query,
      status: nextFilter.status,
      transactionType: nextFilter.transactionType,
      minAmount: nextFilter.minAmount,
      maxAmount: nextFilter.maxAmount,
      occurredFrom: nextFilter.occurredFrom,
      occurredTo: nextFilter.occurredTo,
      sortBy: nextFilter.sortBy,
      sortDir: nextFilter.sortDir,
      page: nextFilter.page,
      size: nextFilter.size,
    });
    startTransition(() => setTransactionsPage(response));
  }

  function navigateWithSearch(pathname: string, search: string) {
    navigate({
      pathname,
      search: search ? `?${search}` : "",
    });
  }

  function handleCustomerSearch() {
    navigateWithSearch("/customers", buildCustomerSearch({ ...customerFilter, page: 0 }));
  }

  function handleCustomerReset() {
    navigateWithSearch("/customers", "");
  }

  function handleCustomerPageChange(page: number, size: number) {
    navigateWithSearch("/customers", buildCustomerSearch({ ...customerSearchFilter, page: page - 1, size }));
  }

  function handleAccountSearch() {
    navigateWithSearch("/accounts", buildAccountSearch({ ...accountFilter, page: 0 }));
  }

  function handleAccountReset() {
    navigateWithSearch("/accounts", "");
  }

  function handleAccountPageChange(page: number, size: number) {
    navigateWithSearch("/accounts", buildAccountSearch({ ...accountSearchFilter, page: page - 1, size }));
  }

  function handleTransactionSearch() {
    navigateWithSearch("/transactions", buildTransactionSearch({ ...transactionFilter, page: 0 }));
  }

  function handleTransactionReset() {
    navigateWithSearch("/transactions", "");
  }

  function handleTransactionPageChange(page: number, size: number) {
    navigateWithSearch("/transactions", buildTransactionSearch({ ...transactionSearchFilter, page: page - 1, size }));
  }

  useEffect(() => {
    if (token) {
      void loadShell(token);
    }
  }, [token]);

  useEffect(() => {
    if (location.pathname === "/customers") {
      const nextFilter = parseCustomerFilter(location.search);
      setCustomerFilter(nextFilter);
      setCustomerSearchFilter(nextFilter);
      return;
    }
    if (location.pathname === "/accounts") {
      const nextFilter = parseAccountFilter(location.search);
      setAccountFilter(nextFilter);
      setAccountSearchFilter(nextFilter);
      return;
    }
    if (location.pathname === "/transactions") {
      const nextFilter = parseTransactionFilter(location.search);
      setTransactionFilter(nextFilter);
      setTransactionSearchFilter(nextFilter);
    }
  }, [location.pathname, location.search, token]);

  useEffect(() => {
    if (!token) {
      return;
    }
    void loadCustomers(token, customerSearchFilter);
    void loadCustomerSummary(token, customerSearchFilter);
  }, [token, customerSearchFilter]);

  useEffect(() => {
    if (!token) {
      return;
    }
    void loadAccounts(token, accountSearchFilter);
  }, [token, accountSearchFilter]);

  useEffect(() => {
    if (!token) {
      return;
    }
    void loadTransactions(token, transactionSearchFilter);
  }, [token, transactionSearchFilter]);

  async function handleLogin(values: { email: string; password: string }) {
    setSubmitting(true);
    try {
      const response = await adminApi.login(values.email, values.password);
      localStorage.setItem(ADMIN_TOKEN_KEY, response.accessToken);
      setToken(response.accessToken);
      messageApi.success(`${response.displayName} 계정으로 로그인했습니다.`);
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : "Login failed");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleApprovalAction() {
    if (!token || !approvalModal || !approvalReason.trim()) {
      return;
    }

    setApprovalSubmitting(true);
    try {
      if (approvalModal.action === "approve") {
        await adminApi.approve(token, approvalModal.id, approvalReason);
      } else {
        await adminApi.reject(token, approvalModal.id, approvalReason);
      }
      await loadShell(token);
      messageApi.success(approvalModal.action === "approve" ? "승인 처리가 완료되었습니다." : "반려 처리가 완료되었습니다.");
      setApprovalModal(null);
      setApprovalReason("");
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : "Approval request failed");
    } finally {
      setApprovalSubmitting(false);
    }
  }

  async function handleCompleteStockOrderFill(orderId: string) {
    if (!token) {
      return;
    }

    setStockFillSubmittingId(orderId);
    try {
      await adminApi.completeStockOrderFill(token, orderId);
      await loadShell(token);
      messageApi.success("잔여 체결 완료 처리가 반영되었습니다.");
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : "Stock order fill completion failed");
    } finally {
      setStockFillSubmittingId(null);
    }
  }

  async function handleBlockLinkedBankAccount(linkedBankAccountId: string) {
    if (!token) {
      return;
    }

    setBlockingLinkedBankAccountId(linkedBankAccountId);
    try {
      await adminApi.blockLinkedBankAccount(token, linkedBankAccountId);
      await loadShell(token);
      messageApi.success("연결 계좌를 차단했습니다.");
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : "Linked bank account block failed");
    } finally {
      setBlockingLinkedBankAccountId(null);
    }
  }

  async function handleActivateLinkedBankAccount(linkedBankAccountId: string) {
    if (!token) {
      return;
    }

    setActivatingLinkedBankAccountId(linkedBankAccountId);
    try {
      await adminApi.activateLinkedBankAccount(token, linkedBankAccountId);
      await loadShell(token);
      messageApi.success("연결 계좌를 활성화했습니다.");
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : "Linked bank account activation failed");
    } finally {
      setActivatingLinkedBankAccountId(null);
    }
  }

  function logout() {
    localStorage.removeItem(ADMIN_TOKEN_KEY);
    setToken(null);
    setProfile(null);
    setOverview(null);
    setAnnouncements([]);
    setApprovals([]);
    setAuditLogs([]);
    setNotifications([]);
    setUnreadNotificationCount(0);
    setFxRates([]);
    setLinkedBankAccounts([]);
    setFundingRequests([]);
    setExchangeRequests([]);
    setStockOrders([]);
    setStockPositions([]);
    setCustomerSummary(null);
    setCustomersPage(null);
    setAccountsPage(null);
    setTransactionsPage(null);
    setCustomerFilter(DEFAULT_CUSTOMER_FILTER);
    setAccountFilter(DEFAULT_ACCOUNT_FILTER);
    setTransactionFilter(DEFAULT_TRANSACTION_FILTER);
    setCustomerSearchFilter(DEFAULT_CUSTOMER_FILTER);
    setAccountSearchFilter(DEFAULT_ACCOUNT_FILTER);
    setTransactionSearchFilter(DEFAULT_TRANSACTION_FILTER);
  }

  return (
    <ConfigProvider theme={{ token: { colorPrimary: "#0f4c81", borderRadius: 14 } }}>
      <AntdApp>
        {contextHolder}
        <Refine>
          <Suspense
            fallback={
              <div className="loading-panel">
                <Spin size="large" />
              </div>
            }
          >
          {!token ? (
            <LoginScreen submitting={submitting} onLogin={handleLogin} />
          ) : shellLoading || !profile || !overview || !customerSummary || !customersPage || !accountsPage || !transactionsPage ? (
            <div className="loading-panel">
              <Spin size="large" />
            </div>
          ) : (
            <AdminShell
              profile={profile}
              unreadNotificationCount={unreadNotificationCount}
              onLogout={logout}
            >
              <Routes>
                <Route
                  path="/"
                  element={
                    <OverviewPage
                      overview={overview}
                      fundingRequests={fundingRequests}
                      announcements={announcements}
                      approvals={approvals}
                      auditLogs={auditLogs}
                    />
                  }
                />
                <Route
                  path="/customers"
                  element={
                    <CustomersPage
                      summary={customerSummary}
                      data={customersPage}
                      filter={customerFilter}
                      setFilter={setCustomerFilter}
                      onSearch={handleCustomerSearch}
                      onReset={handleCustomerReset}
                      onPageChange={handleCustomerPageChange}
                    />
                  }
                />
                <Route
                  path="/accounts"
                  element={
                    <AccountsPage
                      data={accountsPage}
                      filter={accountFilter}
                      setFilter={setAccountFilter}
                      onSearch={handleAccountSearch}
                      onReset={handleAccountReset}
                      onPageChange={handleAccountPageChange}
                    />
                  }
                />
                <Route
                  path="/transactions"
                  element={
                    <TransactionsPage
                      data={transactionsPage}
                      filter={transactionFilter}
                      setFilter={setTransactionFilter}
                      onSearch={handleTransactionSearch}
                      onReset={handleTransactionReset}
                      onPageChange={handleTransactionPageChange}
                    />
                  }
                />
                <Route path="/fx-rates" element={<FxRatesPage rates={fxRates} />} />
                <Route
                  path="/linked-bank-accounts"
                  element={
                    <LinkedBankAccountsPage
                      linkedBankAccounts={linkedBankAccounts}
                      activatingId={activatingLinkedBankAccountId}
                      blockingId={blockingLinkedBankAccountId}
                      onActivate={handleActivateLinkedBankAccount}
                      onBlock={handleBlockLinkedBankAccount}
                    />
                  }
                />
                <Route path="/funding-requests" element={<FundingRequestsPage fundingRequests={fundingRequests} />} />
                <Route path="/exchange-requests" element={<ExchangeRequestsPage exchangeRequests={exchangeRequests} />} />
                <Route
                  path="/stock-orders"
                  element={
                    <StockOrdersPage
                      stockOrders={stockOrders}
                      completingOrderId={stockFillSubmittingId}
                      onCompleteFill={(orderId) => void handleCompleteStockOrderFill(orderId)}
                    />
                  }
                />
                <Route path="/stock-positions" element={<StockPositionsPage stockPositions={stockPositions} />} />
                <Route
                  path="/announcements"
                  element={
                    <AnnouncementsPage
                      announcements={announcements}
                      creatingAnnouncement={creatingAnnouncement}
                      actingAnnouncementId={actingAnnouncementId}
                      onCreate={handleCreateAnnouncement}
                      onUpdate={handleUpdateAnnouncement}
                      onPublish={handlePublishAnnouncement}
                      onArchive={handleArchiveAnnouncement}
                    />
                  }
                />
                <Route
                  path="/approvals"
                  element={
                    <ApprovalsPage
                      approvals={approvals}
                      approvalModal={approvalModal}
                      approvalReason={approvalReason}
                      approvalSubmitting={approvalSubmitting}
                      onOpenModal={setApprovalModal}
                      onCloseModal={() => {
                        setApprovalModal(null);
                        setApprovalReason("");
                      }}
                      onReasonChange={setApprovalReason}
                      onSubmit={() => void handleApprovalAction()}
                    />
                  }
                />
                <Route
                  path="/notifications"
                  element={
                    <NotificationsPage
                      notifications={notifications}
                      unreadCount={unreadNotificationCount}
                      readingNotificationId={readingNotificationId}
                      onRead={(notificationId) => void handleMarkNotificationRead(notificationId)}
                    />
                  }
                />
                <Route path="/audit-logs" element={<AuditLogsPage auditLogs={auditLogs} />} />
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </AdminShell>
          )}
          </Suspense>
        </Refine>
      </AntdApp>
    </ConfigProvider>
  );
}

export default App;
