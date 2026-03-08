import { startTransition, useEffect, useState } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { accountApi } from "./domains/accounts/api";
import { AccountsPage } from "./domains/accounts/AccountsPage";
import type { Account } from "./domains/accounts/types";
import { announcementApi } from "./domains/announcements/api";
import { AnnouncementsPage } from "./domains/announcements/AnnouncementsPage";
import { authApi } from "./domains/auth/api";
import { AuthPage } from "./domains/auth/AuthPage";
import { dashboardApi } from "./domains/dashboard/api";
import { DashboardPage } from "./domains/dashboard/DashboardPage";
import { exchangeApi } from "./domains/exchange/api";
import { ExchangeRequestsPage } from "./domains/exchange/ExchangeRequestsPage";
import type { CreateExchangeRequestPayload, ExchangeRequest } from "./domains/exchange/types";
import { fxApi } from "./domains/fx/api";
import { FxRatesPage } from "./domains/fx/FxRatesPage";
import type { FxRate } from "./domains/fx/types";
import { fundingApi } from "./domains/funding/api";
import { FundingRequestsPage } from "./domains/funding/FundingRequestsPage";
import type { CreateFundingRequestPayload, FundingRequest } from "./domains/funding/types";
import { linkedBankAccountApi } from "./domains/linked-bank-accounts/api";
import { LinkedBankAccountsPage } from "./domains/linked-bank-accounts/LinkedBankAccountsPage";
import type { CreateLinkedBankAccountPayload, LinkedBankAccount } from "./domains/linked-bank-accounts/types";
import { notificationApi } from "./domains/notifications/api";
import { NotificationsPage } from "./domains/notifications/NotificationsPage";
import type { Notification, NotificationList } from "./domains/notifications/types";
import { stockApi } from "./domains/stock/api";
import { StockOrdersPage } from "./domains/stock/StockOrdersPage";
import { StockPositionsPage } from "./domains/stock/StockPositionsPage";
import type { CreateStockOrderPayload, StockOrder, StockPosition } from "./domains/stock/types";
import { transactionApi } from "./domains/transactions/api";
import { TransactionsPage } from "./domains/transactions/TransactionsPage";
import type { Transaction } from "./domains/transactions/types";
import { UserAppLayout } from "./shared/layout/UserAppLayout";
import { USER_TOKEN_KEY } from "./shared/session";
import type { DashboardState } from "./shared/types/dashboard";
import { emptyPageResponse, type PageResponse, type PaginationState } from "./shared/types/page";

const DEFAULT_PAGE_SIZE = 10;
const DASHBOARD_PAGE_SIZE = 200;

function App() {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(USER_TOKEN_KEY));
  const [dashboard, setDashboard] = useState<DashboardState | null>(null);
  const [loading, setLoading] = useState(false);
  const [submittingAction, setSubmittingAction] = useState(false);
  const [cancelingFundingRequestId, setCancelingFundingRequestId] = useState<string | null>(null);
  const [cancelingExchangeRequestId, setCancelingExchangeRequestId] = useState<string | null>(null);
  const [cancelingStockOrderId, setCancelingStockOrderId] = useState<string | null>(null);
  const [verifyingLinkedBankAccountId, setVerifyingLinkedBankAccountId] = useState<string | null>(null);
  const [resendingLinkedBankAccountId, setResendingLinkedBankAccountId] = useState<string | null>(null);
  const [readingNotificationId, setReadingNotificationId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const [accountPagination, setAccountPagination] = useState<PaginationState>({ page: 0, size: DEFAULT_PAGE_SIZE });
  const [linkedBankAccountPagination, setLinkedBankAccountPagination] = useState<PaginationState>({ page: 0, size: DEFAULT_PAGE_SIZE });
  const [fundingRequestPagination, setFundingRequestPagination] = useState<PaginationState>({ page: 0, size: DEFAULT_PAGE_SIZE });
  const [transactionPagination, setTransactionPagination] = useState<PaginationState>({ page: 0, size: DEFAULT_PAGE_SIZE });
  const [fxRatePagination, setFxRatePagination] = useState<PaginationState>({ page: 0, size: DEFAULT_PAGE_SIZE });
  const [exchangeRequestPagination, setExchangeRequestPagination] = useState<PaginationState>({ page: 0, size: DEFAULT_PAGE_SIZE });
  const [stockOrderPagination, setStockOrderPagination] = useState<PaginationState>({ page: 0, size: DEFAULT_PAGE_SIZE });
  const [stockPositionPagination, setStockPositionPagination] = useState<PaginationState>({ page: 0, size: DEFAULT_PAGE_SIZE });
  const [notificationPagination, setNotificationPagination] = useState<PaginationState>({ page: 0, size: DEFAULT_PAGE_SIZE });

  const [accountPage, setAccountPage] = useState<PageResponse<Account>>(emptyPageResponse(DEFAULT_PAGE_SIZE));
  const [linkedBankAccountPage, setLinkedBankAccountPage] = useState<PageResponse<LinkedBankAccount>>(emptyPageResponse(DEFAULT_PAGE_SIZE));
  const [fundingRequestPage, setFundingRequestPage] = useState<PageResponse<FundingRequest>>(emptyPageResponse(DEFAULT_PAGE_SIZE));
  const [transactionPage, setTransactionPage] = useState<PageResponse<Transaction>>(emptyPageResponse(DEFAULT_PAGE_SIZE));
  const [fxRatePage, setFxRatePage] = useState<PageResponse<FxRate>>(emptyPageResponse(DEFAULT_PAGE_SIZE));
  const [exchangeRequestPage, setExchangeRequestPage] = useState<PageResponse<ExchangeRequest>>(emptyPageResponse(DEFAULT_PAGE_SIZE));
  const [stockOrderPage, setStockOrderPage] = useState<PageResponse<StockOrder>>(emptyPageResponse(DEFAULT_PAGE_SIZE));
  const [stockPositionPage, setStockPositionPage] = useState<PageResponse<StockPosition>>(emptyPageResponse(DEFAULT_PAGE_SIZE));
  const [notificationPage, setNotificationPage] = useState<NotificationList>({
    ...emptyPageResponse<Notification>(DEFAULT_PAGE_SIZE),
    unreadCount: 0,
  });

  async function loadDashboard(currentToken: string) {
    setLoading(true);
    setError(null);

    try {
      const dashboardPagination = { page: 0, size: DASHBOARD_PAGE_SIZE };
      const [
        profile,
        insights,
        announcements,
        notifications,
        accounts,
        linkedBankAccounts,
        fundingRequests,
        transactions,
        fxRates,
        exchangeRequests,
        stockOrders,
        stockPositions,
      ] = await Promise.all([
        authApi.me(currentToken),
        dashboardApi.insights(currentToken),
        announcementApi.list(currentToken),
        notificationApi.list(currentToken, dashboardPagination),
        accountApi.list(currentToken, dashboardPagination),
        linkedBankAccountApi.list(currentToken, dashboardPagination),
        fundingApi.list(currentToken, dashboardPagination),
        transactionApi.list(currentToken, dashboardPagination),
        fxApi.list(currentToken, dashboardPagination),
        exchangeApi.list(currentToken, dashboardPagination),
        stockApi.orders(currentToken, dashboardPagination),
        stockApi.positions(currentToken, dashboardPagination),
      ]);

      startTransition(() => {
        setDashboard({
          profile,
          insights,
          announcements,
          accounts: accounts.items,
          linkedBankAccounts: linkedBankAccounts.items,
          fundingRequests: fundingRequests.items,
          transactions: transactions.items,
          fxRates: fxRates.items,
          exchangeRequests: exchangeRequests.items,
          stockOrders: stockOrders.items,
          stockPositions: stockPositions.items,
          notifications: notifications.items,
          unreadNotificationCount: notifications.unreadCount,
        });
      });
    } catch (requestError) {
      localStorage.removeItem(USER_TOKEN_KEY);
      setToken(null);
      setDashboard(null);
      setError(requestError instanceof Error ? requestError.message : "Session expired");
    } finally {
      setLoading(false);
    }
  }

  async function loadAccountPage(currentToken: string, pagination = accountPagination) {
    setAccountPage(await accountApi.list(currentToken, pagination));
  }

  async function loadLinkedBankAccountPage(currentToken: string, pagination = linkedBankAccountPagination) {
    setLinkedBankAccountPage(await linkedBankAccountApi.list(currentToken, pagination));
  }

  async function loadFundingRequestPage(currentToken: string, pagination = fundingRequestPagination) {
    setFundingRequestPage(await fundingApi.list(currentToken, pagination));
  }

  async function loadTransactionPage(currentToken: string, pagination = transactionPagination) {
    setTransactionPage(await transactionApi.list(currentToken, pagination));
  }

  async function loadFxRatePage(currentToken: string, pagination = fxRatePagination) {
    setFxRatePage(await fxApi.list(currentToken, pagination));
  }

  async function loadExchangeRequestPage(currentToken: string, pagination = exchangeRequestPagination) {
    setExchangeRequestPage(await exchangeApi.list(currentToken, pagination));
  }

  async function loadStockOrderPage(currentToken: string, pagination = stockOrderPagination) {
    setStockOrderPage(await stockApi.orders(currentToken, pagination));
  }

  async function loadStockPositionPage(currentToken: string, pagination = stockPositionPagination) {
    setStockPositionPage(await stockApi.positions(currentToken, pagination));
  }

  async function loadNotificationPage(currentToken: string, pagination = notificationPagination) {
    setNotificationPage(await notificationApi.list(currentToken, pagination));
  }

  async function refreshPagedResources(currentToken: string) {
    await Promise.all([
      loadAccountPage(currentToken),
      loadLinkedBankAccountPage(currentToken),
      loadFundingRequestPage(currentToken),
      loadTransactionPage(currentToken),
      loadFxRatePage(currentToken),
      loadExchangeRequestPage(currentToken),
      loadStockOrderPage(currentToken),
      loadStockPositionPage(currentToken),
      loadNotificationPage(currentToken),
    ]);
  }

  useEffect(() => {
    if (token) {
      void loadDashboard(token);
    }
  }, [token]);

  useEffect(() => {
    if (token) {
      void loadAccountPage(token);
    }
  }, [token, accountPagination.page, accountPagination.size]);

  useEffect(() => {
    if (token) {
      void loadLinkedBankAccountPage(token);
    }
  }, [token, linkedBankAccountPagination.page, linkedBankAccountPagination.size]);

  useEffect(() => {
    if (token) {
      void loadFundingRequestPage(token);
    }
  }, [token, fundingRequestPagination.page, fundingRequestPagination.size]);

  useEffect(() => {
    if (token) {
      void loadTransactionPage(token);
    }
  }, [token, transactionPagination.page, transactionPagination.size]);

  useEffect(() => {
    if (token) {
      void loadFxRatePage(token);
    }
  }, [token, fxRatePagination.page, fxRatePagination.size]);

  useEffect(() => {
    if (token) {
      void loadExchangeRequestPage(token);
    }
  }, [token, exchangeRequestPagination.page, exchangeRequestPagination.size]);

  useEffect(() => {
    if (token) {
      void loadStockOrderPage(token);
    }
  }, [token, stockOrderPagination.page, stockOrderPagination.size]);

  useEffect(() => {
    if (token) {
      void loadStockPositionPage(token);
    }
  }, [token, stockPositionPagination.page, stockPositionPagination.size]);

  useEffect(() => {
    if (token) {
      void loadNotificationPage(token);
    }
  }, [token, notificationPagination.page, notificationPagination.size]);

  async function login(email: string, password: string) {
    setLoading(true);
    setError(null);

    try {
      const response = await authApi.login(email, password);
      localStorage.setItem(USER_TOKEN_KEY, response.accessToken);
      setToken(response.accessToken);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Authentication failed");
      setLoading(false);
    }
  }

  async function signup(email: string, password: string, fullName: string) {
    setLoading(true);
    setError(null);

    try {
      const response = await authApi.signup(email, password, fullName);
      localStorage.setItem(USER_TOKEN_KEY, response.accessToken);
      setToken(response.accessToken);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Authentication failed");
      setLoading(false);
    }
  }

  function logout() {
    localStorage.removeItem(USER_TOKEN_KEY);
    setToken(null);
    setDashboard(null);
    setError(null);
  }

  async function createExchangeRequest(payload: CreateExchangeRequestPayload) {
    if (!token) {
      return;
    }

    setSubmittingAction(true);
    setError(null);
    try {
      await exchangeApi.create(token, payload);
      await Promise.all([loadDashboard(token), refreshPagedResources(token)]);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Failed to create exchange request");
    } finally {
      setSubmittingAction(false);
    }
  }

  async function cancelExchangeRequest(requestId: string, reason?: string) {
    if (!token) {
      return;
    }

    setCancelingExchangeRequestId(requestId);
    setError(null);
    try {
      await exchangeApi.cancel(token, requestId, { reason });
      await Promise.all([loadDashboard(token), refreshPagedResources(token)]);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Failed to cancel exchange request");
    } finally {
      setCancelingExchangeRequestId(null);
    }
  }

  async function createFundingRequest(payload: CreateFundingRequestPayload) {
    if (!token) {
      return;
    }

    setSubmittingAction(true);
    setError(null);
    try {
      await fundingApi.create(token, payload);
      await Promise.all([loadDashboard(token), refreshPagedResources(token)]);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Failed to create funding request");
    } finally {
      setSubmittingAction(false);
    }
  }

  async function cancelFundingRequest(requestId: string, reason?: string) {
    if (!token) {
      return;
    }

    setCancelingFundingRequestId(requestId);
    setError(null);
    try {
      await fundingApi.cancel(token, requestId, { reason });
      await Promise.all([loadDashboard(token), refreshPagedResources(token)]);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Failed to cancel funding request");
    } finally {
      setCancelingFundingRequestId(null);
    }
  }

  async function createLinkedBankAccount(payload: CreateLinkedBankAccountPayload) {
    if (!token) {
      return;
    }

    setSubmittingAction(true);
    setError(null);
    try {
      await linkedBankAccountApi.create(token, payload);
      await Promise.all([loadDashboard(token), refreshPagedResources(token)]);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Failed to create linked bank account");
    } finally {
      setSubmittingAction(false);
    }
  }

  async function markPrimaryLinkedBankAccount(linkedBankAccountId: string) {
    if (!token) {
      return;
    }

    setSubmittingAction(true);
    setError(null);
    try {
      await linkedBankAccountApi.markPrimary(token, linkedBankAccountId);
      await Promise.all([loadDashboard(token), refreshPagedResources(token)]);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Failed to update primary linked bank account");
    } finally {
      setSubmittingAction(false);
    }
  }

  async function verifyLinkedBankAccount(linkedBankAccountId: string, verificationReference: string) {
    if (!token) {
      return;
    }

    setVerifyingLinkedBankAccountId(linkedBankAccountId);
    setError(null);
    try {
      await linkedBankAccountApi.verify(token, linkedBankAccountId, { verificationReference });
      await Promise.all([loadDashboard(token), refreshPagedResources(token)]);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Failed to verify linked bank account");
    } finally {
      setVerifyingLinkedBankAccountId(null);
    }
  }

  async function resendLinkedBankAccountVerification(linkedBankAccountId: string) {
    if (!token) {
      return;
    }

    setResendingLinkedBankAccountId(linkedBankAccountId);
    setError(null);
    try {
      await linkedBankAccountApi.resendVerification(token, linkedBankAccountId);
      await Promise.all([loadDashboard(token), refreshPagedResources(token)]);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Failed to resend linked bank account verification");
    } finally {
      setResendingLinkedBankAccountId(null);
    }
  }

  async function createStockOrder(payload: CreateStockOrderPayload) {
    if (!token) {
      return;
    }

    setSubmittingAction(true);
    setError(null);
    try {
      await stockApi.create(token, payload);
      await Promise.all([loadDashboard(token), refreshPagedResources(token)]);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Failed to create stock order");
    } finally {
      setSubmittingAction(false);
    }
  }

  async function cancelStockOrder(orderId: string, reason?: string) {
    if (!token) {
      return;
    }

    setCancelingStockOrderId(orderId);
    setError(null);
    try {
      await stockApi.cancel(token, orderId, { reason });
      await Promise.all([loadDashboard(token), refreshPagedResources(token)]);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Failed to cancel stock order");
    } finally {
      setCancelingStockOrderId(null);
    }
  }

  async function markNotificationRead(notificationId: string) {
    if (!token) {
      return;
    }

    setReadingNotificationId(notificationId);
    setError(null);
    try {
      const updated = await notificationApi.markRead(token, notificationId);
      startTransition(() => {
        setDashboard((current) => {
          if (!current) {
            return current;
          }

          const target = current.notifications.find((item) => item.id === notificationId);
          return {
            ...current,
            notifications: current.notifications.map((item) => (item.id === notificationId ? updated : item)),
            unreadNotificationCount: target && !target.read
              ? Math.max(0, current.unreadNotificationCount - 1)
              : current.unreadNotificationCount,
          };
        });
        setNotificationPage((current) => {
          const target = current.items.find((item) => item.id === notificationId);
          return {
            ...current,
            items: current.items.map((item) => (item.id === notificationId ? updated : item)),
            unreadCount: target && !target.read ? Math.max(0, current.unreadCount - 1) : current.unreadCount,
          };
        });
      });
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Failed to update notification");
    } finally {
      setReadingNotificationId(null);
    }
  }

  if (!token) {
    return <AuthPage loading={loading} error={error} onLogin={login} onSignup={signup} />;
  }

  const accounts = dashboard?.accounts ?? [];
  const linkedBankAccounts = dashboard?.linkedBankAccounts ?? [];
  const fundingRequests = dashboard?.fundingRequests ?? [];
  const transactions = dashboard?.transactions ?? [];
  const fxRates = dashboard?.fxRates ?? [];
  const exchangeRequests = dashboard?.exchangeRequests ?? [];
  const stockOrders = dashboard?.stockOrders ?? [];
  const stockPositions = dashboard?.stockPositions ?? [];
  const notifications = dashboard?.notifications ?? [];

  return (
    <Routes>
      <Route
        path="/"
        element={
          <UserAppLayout
            profile={dashboard?.profile}
            loading={loading}
            error={error}
            unreadNotificationCount={dashboard?.unreadNotificationCount ?? 0}
            announcements={dashboard?.announcements ?? []}
            onLogout={logout}
          />
        }
      >
        <Route index element={<DashboardPage loading={loading} dashboard={dashboard} />} />
        <Route path="announcements" element={<AnnouncementsPage loading={loading} announcements={dashboard?.announcements ?? []} />} />
        <Route
          path="accounts"
          element={
            <AccountsPage
              loading={loading}
              accounts={accounts}
              accountRows={accountPage.items}
              accountPage={accountPage}
              onAccountPageChange={(page, pageSize) => setAccountPagination({ page: page - 1, size: pageSize })}
              transactions={transactions}
            />
          }
        />
        <Route
          path="linked-bank-accounts"
          element={
            <LinkedBankAccountsPage
              loading={loading}
              submitting={submittingAction}
              verifyingId={verifyingLinkedBankAccountId}
              resendingId={resendingLinkedBankAccountId}
              linkedBankAccounts={linkedBankAccounts}
              linkedBankAccountRows={linkedBankAccountPage.items}
              linkedBankAccountPage={linkedBankAccountPage}
              onLinkedBankAccountPageChange={(page, pageSize) => setLinkedBankAccountPagination({ page: page - 1, size: pageSize })}
              onCreate={createLinkedBankAccount}
              onMarkPrimary={markPrimaryLinkedBankAccount}
              onVerify={verifyLinkedBankAccount}
              onResend={resendLinkedBankAccountVerification}
            />
          }
        />
        <Route
          path="funding-requests"
          element={
            <FundingRequestsPage
              loading={loading}
              submitting={submittingAction}
              cancelingRequestId={cancelingFundingRequestId}
              accounts={accounts}
              linkedBankAccounts={linkedBankAccounts}
              fundingRequests={fundingRequests}
              fundingRequestRows={fundingRequestPage.items}
              fundingRequestPage={fundingRequestPage}
              onFundingRequestPageChange={(page, pageSize) => setFundingRequestPagination({ page: page - 1, size: pageSize })}
              onCreate={createFundingRequest}
              onCancel={cancelFundingRequest}
            />
          }
        />
        <Route
          path="transactions"
          element={
            <TransactionsPage
              loading={loading}
              transactions={transactions}
              transactionRows={transactionPage.items}
              transactionPage={transactionPage}
              onTransactionPageChange={(page, pageSize) => setTransactionPagination({ page: page - 1, size: pageSize })}
            />
          }
        />
        <Route
          path="fx-rates"
          element={
            <FxRatesPage
              loading={loading}
              fxRates={fxRates}
              fxRateRows={fxRatePage.items}
              fxRatePage={fxRatePage}
              onFxRatePageChange={(page, pageSize) => setFxRatePagination({ page: page - 1, size: pageSize })}
            />
          }
        />
        <Route
          path="exchange-requests"
          element={
            <ExchangeRequestsPage
              loading={loading}
              submitting={submittingAction}
              cancelingRequestId={cancelingExchangeRequestId}
              accounts={accounts}
              fxRates={fxRates as FxRate[]}
              exchangeRequests={exchangeRequests}
              exchangeRequestRows={exchangeRequestPage.items}
              exchangeRequestPage={exchangeRequestPage}
              onExchangeRequestPageChange={(page, pageSize) => setExchangeRequestPagination({ page: page - 1, size: pageSize })}
              onCreate={createExchangeRequest}
              onCancel={cancelExchangeRequest}
            />
          }
        />
        <Route
          path="stock-orders"
          element={
            <StockOrdersPage
              loading={loading}
              submitting={submittingAction}
              cancelingOrderId={cancelingStockOrderId}
              accounts={accounts}
              stockPositions={stockPositions}
              stockOrders={stockOrders}
              stockOrderRows={stockOrderPage.items}
              stockOrderPage={stockOrderPage}
              onStockOrderPageChange={(page, pageSize) => setStockOrderPagination({ page: page - 1, size: pageSize })}
              onCreate={createStockOrder}
              onCancel={cancelStockOrder}
            />
          }
        />
        <Route
          path="stock-positions"
          element={
            <StockPositionsPage
              loading={loading}
              stockPositions={stockPositions}
              stockPositionRows={stockPositionPage.items}
              stockPositionPage={stockPositionPage}
              onStockPositionPageChange={(page, pageSize) => setStockPositionPagination({ page: page - 1, size: pageSize })}
            />
          }
        />
        <Route
          path="notifications"
          element={
            <NotificationsPage
              loading={loading}
              notifications={notifications}
              notificationRows={notificationPage.items}
              notificationPage={notificationPage}
              onNotificationPageChange={(page, pageSize) => setNotificationPagination({ page: page - 1, size: pageSize })}
              unreadCount={dashboard?.unreadNotificationCount ?? 0}
              readingNotificationId={readingNotificationId}
              onRead={markNotificationRead}
            />
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}

export default App;
