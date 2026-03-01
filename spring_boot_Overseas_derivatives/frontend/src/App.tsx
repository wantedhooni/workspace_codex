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
import { MenusPage } from "./pages/MenusPage";
import { ApprovalPoliciesPage } from "./pages/ApprovalPoliciesPage";
import { RiskLimitsPage } from "./pages/RiskLimitsPage";
import { OpsCasesPage } from "./pages/OpsCasesPage";
import { OpsCaseDetailPage } from "./pages/OpsCaseDetailPage";
import { PortfolioPage } from "./pages/PortfolioPage";
import { StockPurchasesPage } from "./pages/StockPurchasesPage";
import { StockPositionsPage } from "./pages/StockPositionsPage";
import { DomainTermsPage } from "./pages/DomainTermsPage";
import { StockRecommendationsPage } from "./pages/StockRecommendationsPage";
import { ExchangeRatesPage } from "./pages/ExchangeRatesPage";

export function App() {
  return (
    <RefineSnackbarProvider>
      <Refine
        dataProvider={dataProvider(import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1")}
        authProvider={authProvider}
        routerProvider={routerBindings}
        resources={[
          { name: "dashboard", list: "/" },
          { name: "domain-terms", list: "/domain-terms" },
          { name: "portfolio", list: "/portfolio" },
          { name: "stock-purchases", list: "/stock-purchases" },
          { name: "stock-positions", list: "/stock-positions" },
          { name: "stock-recommendations", list: "/stock-recommendations" },
          { name: "exchange-rates", list: "/exchange-rates" },
          { name: "accounts", list: "/accounts" },
          { name: "cash-requests", list: "/cash-requests", show: "/cash-requests/:id" },
          { name: "fx-requests", list: "/fx-requests", show: "/fx-requests/:id" },
          { name: "batches", list: "/batches" },
          { name: "audit-logs", list: "/audit-logs" },
          { name: "approval-policies", list: "/approval-policies" },
          { name: "risk-limits", list: "/risk-limits" },
          { name: "ops-cases", list: "/ops-cases", show: "/ops-cases/:id" },
          { name: "menus", list: "/menus" },
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
            <Route path="/domain-terms" element={<DomainTermsPage />} />
            <Route path="/portfolio" element={<PortfolioPage />} />
            <Route path="/stock-purchases" element={<StockPurchasesPage />} />
            <Route path="/stock-positions" element={<StockPositionsPage />} />
            <Route path="/stock-recommendations" element={<StockRecommendationsPage />} />
            <Route path="/exchange-rates" element={<ExchangeRatesPage />} />
            <Route path="/accounts" element={<AccountsPage />} />
            <Route path="/accounts/:id" element={<AccountSummaryPage />} />
            <Route path="/cash-requests" element={<CashRequestsPage />} />
            <Route path="/cash-requests/:id" element={<CashRequestDetailPage />} />
            <Route path="/fx-requests" element={<FxRequestsPage />} />
            <Route path="/fx-requests/:id" element={<FxRequestDetailPage />} />
            <Route path="/batches" element={<BatchRunsPage />} />
            <Route path="/audit-logs" element={<AuditLogsPage />} />
            <Route path="/approval-policies" element={<ApprovalPoliciesPage />} />
            <Route path="/risk-limits" element={<RiskLimitsPage />} />
            <Route path="/ops-cases" element={<OpsCasesPage />} />
            <Route path="/ops-cases/:id" element={<OpsCaseDetailPage />} />
            <Route path="/menus" element={<MenusPage />} />
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
