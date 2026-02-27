import { Authenticated, Refine } from "@refinedev/core";
import routerBindings, { CatchAllNavigate, NavigateToResource } from "@refinedev/react-router-v6";
import { RefineSnackbarProvider } from "@refinedev/mui";
import dataProvider from "@refinedev/simple-rest";
import { Route, Routes } from "react-router-dom";
import { authProvider } from "./auth/authProvider";
import { AppLayout } from "./layout/AppLayout";
import { LoginPage } from "./pages/LoginPage";
import { DashboardPage } from "./pages/DashboardPage";
import { AccountsPage } from "./pages/AccountsPage";
import { AccountSummaryPage } from "./pages/AccountSummaryPage";
import { CashRequestsPage } from "./pages/CashRequestsPage";
import { FxRequestsPage } from "./pages/FxRequestsPage";
import { BatchRunsPage } from "./pages/BatchRunsPage";
import { AuditLogsPage } from "./pages/AuditLogsPage";
import { CashRequestDetailPage } from "./pages/CashRequestDetailPage";
import { FxRequestDetailPage } from "./pages/FxRequestDetailPage";

export function App() {
  return (
    <RefineSnackbarProvider>
      <Refine
        dataProvider={dataProvider(import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1")}
        authProvider={authProvider}
        routerProvider={routerBindings}
        resources={[
          { name: "dashboard", list: "/" },
          { name: "accounts", list: "/accounts" },
          { name: "cash-requests", list: "/cash-requests", show: "/cash-requests/:id" },
          { name: "fx-requests", list: "/fx-requests", show: "/fx-requests/:id" },
          { name: "batches", list: "/batches" },
          { name: "audit-logs", list: "/audit-logs" },
        ]}
        options={{ syncWithLocation: true, warnWhenUnsavedChanges: false }}
      >
        <Routes>
          <Route
            element={
              <Authenticated key="protected" fallback={<CatchAllNavigate to="/login" />}>
                <AppLayout />
              </Authenticated>
            }
          >
            <Route index element={<DashboardPage />} />
            <Route path="/accounts" element={<AccountsPage />} />
            <Route path="/accounts/:id" element={<AccountSummaryPage />} />
            <Route path="/cash-requests" element={<CashRequestsPage />} />
            <Route path="/cash-requests/:id" element={<CashRequestDetailPage />} />
            <Route path="/fx-requests" element={<FxRequestsPage />} />
            <Route path="/fx-requests/:id" element={<FxRequestDetailPage />} />
            <Route path="/batches" element={<BatchRunsPage />} />
            <Route path="/audit-logs" element={<AuditLogsPage />} />
          </Route>

          <Route
            path="/login"
            element={
              <Authenticated key="login" fallback={<LoginPage />}>
                <NavigateToResource resource="dashboard" />
              </Authenticated>
            }
          />

          <Route path="*" element={<CatchAllNavigate to="/" />} />
        </Routes>
      </Refine>
    </RefineSnackbarProvider>
  );
}
