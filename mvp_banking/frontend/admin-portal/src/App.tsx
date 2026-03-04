import { Suspense, lazy, startTransition, useDeferredValue, useEffect, useState } from "react";
import { Refine } from "@refinedev/core";
import { App as AntdApp, ConfigProvider, Spin, message } from "antd";
import { Navigate, Route, Routes } from "react-router-dom";
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

function App() {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(ADMIN_TOKEN_KEY));
  const [submitting, setSubmitting] = useState(false);
  const [shellLoading, setShellLoading] = useState(false);
  const [approvalSubmitting, setApprovalSubmitting] = useState(false);
  const [stockFillSubmittingId, setStockFillSubmittingId] = useState<string | null>(null);
  const [profile, setProfile] = useState<Profile | null>(null);
  const [overview, setOverview] = useState<AdminOverview | null>(null);
  const [announcements, setAnnouncements] = useState<Announcement[]>([]);
  const [approvals, setApprovals] = useState<Approval[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadNotificationCount, setUnreadNotificationCount] = useState(0);
  const [fxRates, setFxRates] = useState<FxRate[]>([]);
  const [fundingRequests, setFundingRequests] = useState<FundingRequest[]>([]);
  const [exchangeRequests, setExchangeRequests] = useState<ExchangeRequest[]>([]);
  const [stockOrders, setStockOrders] = useState<StockOrder[]>([]);
  const [stockPositions, setStockPositions] = useState<StockPosition[]>([]);
  const [customerSummary, setCustomerSummary] = useState<CustomerStatusSummary | null>(null);
  const [customersPage, setCustomersPage] = useState<PageResponse<Customer> | null>(null);
  const [accountsPage, setAccountsPage] = useState<PageResponse<Account> | null>(null);
  const [transactionsPage, setTransactionsPage] = useState<PageResponse<Transaction> | null>(null);
  const [customerFilter, setCustomerFilter] = useState<CustomerFilter>({
    query: "",
    sortBy: "createdAt",
    sortDir: "desc",
    page: 0,
    size: 5,
  });
  const [accountFilter, setAccountFilter] = useState<AccountFilter>({
    query: "",
    sortBy: "createdAt",
    sortDir: "desc",
    page: 0,
    size: 5,
  });
  const [transactionFilter, setTransactionFilter] = useState<TransactionFilter>({
    query: "",
    sortBy: "occurredAt",
    sortDir: "desc",
    page: 0,
    size: 5,
  });
  const [approvalModal, setApprovalModal] = useState<{ id: string; action: "approve" | "reject" } | null>(null);
  const [approvalReason, setApprovalReason] = useState("");
  const [readingNotificationId, setReadingNotificationId] = useState<string | null>(null);
  const [creatingAnnouncement, setCreatingAnnouncement] = useState(false);
  const [actingAnnouncementId, setActingAnnouncementId] = useState<string | null>(null);
  const [messageApi, contextHolder] = message.useMessage();

  const deferredCustomerQuery = useDeferredValue(customerFilter.query);
  const deferredAccountQuery = useDeferredValue(accountFilter.query);
  const deferredTransactionQuery = useDeferredValue(transactionFilter.query);

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

  useEffect(() => {
    if (token) {
      void loadShell(token);
    }
  }, [token]);

  useEffect(() => {
    if (!token) {
      return;
    }
    void loadCustomers(token, { ...customerFilter, query: deferredCustomerQuery });
    void loadCustomerSummary(token, { ...customerFilter, query: deferredCustomerQuery });
  }, [
    token,
    deferredCustomerQuery,
    customerFilter.status,
    customerFilter.createdFrom,
    customerFilter.createdTo,
    customerFilter.sortBy,
    customerFilter.sortDir,
    customerFilter.page,
    customerFilter.size,
  ]);

  useEffect(() => {
    if (!token) {
      return;
    }
    void loadAccounts(token, { ...accountFilter, query: deferredAccountQuery });
  }, [
    token,
    deferredAccountQuery,
    accountFilter.status,
    accountFilter.accountType,
    accountFilter.minBalance,
    accountFilter.maxBalance,
    accountFilter.sortBy,
    accountFilter.sortDir,
    accountFilter.page,
    accountFilter.size,
  ]);

  useEffect(() => {
    if (!token) {
      return;
    }
    void loadTransactions(token, { ...transactionFilter, query: deferredTransactionQuery });
  }, [
    token,
    deferredTransactionQuery,
    transactionFilter.status,
    transactionFilter.transactionType,
    transactionFilter.minAmount,
    transactionFilter.maxAmount,
    transactionFilter.occurredFrom,
    transactionFilter.occurredTo,
    transactionFilter.sortBy,
    transactionFilter.sortDir,
    transactionFilter.page,
    transactionFilter.size,
  ]);

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
    setFundingRequests([]);
    setExchangeRequests([]);
    setStockOrders([]);
    setStockPositions([]);
    setCustomerSummary(null);
    setCustomersPage(null);
    setAccountsPage(null);
    setTransactionsPage(null);
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
                    />
                  }
                />
                <Route path="/fx-rates" element={<FxRatesPage rates={fxRates} />} />
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
