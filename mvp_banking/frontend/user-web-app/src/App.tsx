import { startTransition, useEffect, useState } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { accountApi } from "./domains/accounts/api";
import { AccountsPage } from "./domains/accounts/AccountsPage";
import { announcementApi } from "./domains/announcements/api";
import { AnnouncementsPage } from "./domains/announcements/AnnouncementsPage";
import { authApi } from "./domains/auth/api";
import { AuthPage } from "./domains/auth/AuthPage";
import { dashboardApi } from "./domains/dashboard/api";
import { DashboardPage } from "./domains/dashboard/DashboardPage";
import { exchangeApi } from "./domains/exchange/api";
import { ExchangeRequestsPage } from "./domains/exchange/ExchangeRequestsPage";
import type { CreateExchangeRequestPayload } from "./domains/exchange/types";
import { fxApi } from "./domains/fx/api";
import { FxRatesPage } from "./domains/fx/FxRatesPage";
import type { FxRate } from "./domains/fx/types";
import { fundingApi } from "./domains/funding/api";
import { FundingRequestsPage } from "./domains/funding/FundingRequestsPage";
import type { CreateFundingRequestPayload } from "./domains/funding/types";
import { linkedBankAccountApi } from "./domains/linked-bank-accounts/api";
import { LinkedBankAccountsPage } from "./domains/linked-bank-accounts/LinkedBankAccountsPage";
import type { CreateLinkedBankAccountPayload } from "./domains/linked-bank-accounts/types";
import { notificationApi } from "./domains/notifications/api";
import { NotificationsPage } from "./domains/notifications/NotificationsPage";
import { stockApi } from "./domains/stock/api";
import { StockOrdersPage } from "./domains/stock/StockOrdersPage";
import { StockPositionsPage } from "./domains/stock/StockPositionsPage";
import type { CreateStockOrderPayload } from "./domains/stock/types";
import { transactionApi } from "./domains/transactions/api";
import { TransactionsPage } from "./domains/transactions/TransactionsPage";
import { UserAppLayout } from "./shared/layout/UserAppLayout";
import { USER_TOKEN_KEY } from "./shared/session";
import type { DashboardState } from "./shared/types/dashboard";

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

  async function loadDashboard(currentToken: string) {
    setLoading(true);
    setError(null);

    try {
      const [profile, insights, announcements, notifications, accounts, linkedBankAccounts, fundingRequests, transactions, fxRates, exchangeRequests, stockOrders, stockPositions] = await Promise.all([
        authApi.me(currentToken),
        dashboardApi.insights(currentToken),
        announcementApi.list(currentToken),
        notificationApi.list(currentToken),
        accountApi.list(currentToken),
        linkedBankAccountApi.list(currentToken),
        fundingApi.list(currentToken),
        transactionApi.list(currentToken),
        fxApi.list(currentToken),
        exchangeApi.list(currentToken),
        stockApi.orders(currentToken),
        stockApi.positions(currentToken),
      ]);

      startTransition(() => {
        setDashboard({
          profile,
          insights,
          announcements,
          accounts,
          linkedBankAccounts,
          fundingRequests,
          transactions,
          fxRates,
          exchangeRequests,
          stockOrders,
          stockPositions,
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

  useEffect(() => {
    if (token) {
      void loadDashboard(token);
    }
  }, [token]);

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
      await loadDashboard(token);
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
      await loadDashboard(token);
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
      await loadDashboard(token);
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
      await loadDashboard(token);
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
      await loadDashboard(token);
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
      await loadDashboard(token);
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
      await loadDashboard(token);
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
      await loadDashboard(token);
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
      await loadDashboard(token);
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
      await loadDashboard(token);
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
        <Route path="accounts" element={<AccountsPage loading={loading} accounts={accounts} transactions={transactions} />} />
        <Route
          path="linked-bank-accounts"
          element={
            <LinkedBankAccountsPage
              loading={loading}
              submitting={submittingAction}
              verifyingId={verifyingLinkedBankAccountId}
              resendingId={resendingLinkedBankAccountId}
              linkedBankAccounts={linkedBankAccounts}
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
              onCreate={createFundingRequest}
              onCancel={cancelFundingRequest}
            />
          }
        />
        <Route path="transactions" element={<TransactionsPage loading={loading} transactions={transactions} />} />
        <Route path="fx-rates" element={<FxRatesPage loading={loading} fxRates={fxRates} />} />
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
              onCreate={createStockOrder}
              onCancel={cancelStockOrder}
            />
          }
        />
        <Route path="stock-positions" element={<StockPositionsPage loading={loading} stockPositions={stockPositions} />} />
        <Route
          path="notifications"
          element={
            <NotificationsPage
              loading={loading}
              notifications={dashboard?.notifications ?? []}
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
