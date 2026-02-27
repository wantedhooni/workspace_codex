import { Admin, Resource } from "react-admin";
import { dataProvider } from "./providers/dataProvider";
import { authProvider, type AppPermissions } from "./providers/authProvider";
import Dashboard from "./pages/Dashboard";
import { LoginPage } from "./pages/LoginPage";
import { OrderList } from "./pages/OrderList";
import { OrderAuditList } from "./pages/OrderAuditList";
import { TradeList } from "./pages/TradeList";
import { PositionList } from "./pages/PositionList";
import { RiskLimitList } from "./pages/RiskLimitList";
import { RiskAlertList } from "./pages/RiskAlertList";
import { ExecutionQualityList } from "./pages/ExecutionQualityList";
import { OrderHealthList } from "./pages/OrderHealthList";
import { PortfolioSummaryList } from "./pages/PortfolioSummaryList";
import { VoucherList } from "./pages/VoucherList";
import { LedgerEntryList } from "./pages/LedgerEntryList";
import { UserList } from "./pages/UserList";
import { RoleList } from "./pages/RoleList";
import { MenuList } from "./pages/MenuList";
import { MenuPermissionList } from "./pages/MenuPermissionList";
import { AccountProfileList } from "./pages/AccountProfileList";
import { AccountSessionList } from "./pages/AccountSessionList";
import { quantDarkTheme, quantTheme } from "./theme/quantTheme";
import { QuantLayout } from "./layout/QuantLayout";

const canRead = (permissions: AppPermissions | undefined, menuKey: string) =>
  Boolean(permissions?.menuPermissions?.[menuKey]?.canRead);

export default function App() {
  return (
    <Admin
      title="Quant Admin"
      layout={QuantLayout}
      dataProvider={dataProvider}
      authProvider={authProvider}
      loginPage={LoginPage}
      dashboard={Dashboard}
      theme={quantTheme}
      darkTheme={quantDarkTheme}
      defaultTheme="light"
    >
      {(permissions) => {
        const p = permissions as AppPermissions | undefined;
        return (
          <>
            {canRead(p, "orders") ? (
              <Resource name="orders" list={OrderList} options={{ label: "주문" }} />
            ) : null}

            {canRead(p, "orderAudits") ? (
              <Resource name="orderAudits" list={OrderAuditList} options={{ label: "주문감사" }} />
            ) : null}

            {canRead(p, "trades") ? (
              <Resource name="trades" list={TradeList} options={{ label: "체결" }} />
            ) : null}

            {canRead(p, "positions") ? <Resource name="positions" list={PositionList} options={{ label: "포지션" }} /> : null}

            {canRead(p, "portfolioSummaries") ? (
              <Resource name="portfolioSummaries" list={PortfolioSummaryList} options={{ label: "포트폴리오 요약" }} />
            ) : null}

            {canRead(p, "orderHealth") ? (
              <Resource
                name="orderHealth"
                list={OrderHealthList}
                options={{ label: "주문건전성" }}
              />
            ) : null}

            {canRead(p, "riskLimits") ? (
              <Resource
                name="riskLimits"
                list={RiskLimitList}
                options={{ label: "리스크 한도" }}
              />
            ) : null}

            {canRead(p, "riskAlerts") ? (
              <Resource
                name="riskAlerts"
                list={RiskAlertList}
                options={{ label: "리스크 경보" }}
              />
            ) : null}

            {canRead(p, "executionQualities") ? (
              <Resource
                name="executionQualities"
                list={ExecutionQualityList}
                options={{ label: "체결품질" }}
              />
            ) : null}

            {canRead(p, "users") ? (
              <Resource name="users" list={UserList} options={{ label: "사용자" }} />
            ) : null}

            {canRead(p, "roles") ? (
              <Resource name="roles" list={RoleList} options={{ label: "권한" }} />
            ) : null}

            {canRead(p, "menus") ? (
              <Resource name="menus" list={MenuList} options={{ label: "메뉴" }} />
            ) : null}

            {canRead(p, "menuPermissions") ? (
              <Resource
                name="menuPermissions"
                list={MenuPermissionList}
                options={{ label: "메뉴권한" }}
              />
            ) : null}

            {canRead(p, "journalVouchers") ? (
              <Resource
                name="journalVouchers"
                list={VoucherList}
                options={{ label: "전표" }}
              />
            ) : null}

            {canRead(p, "ledgerEntries") ? (
              <Resource name="ledgerEntries" list={LedgerEntryList} options={{ label: "원장" }} />
            ) : null}

            {canRead(p, "accountProfile") ? (
              <Resource name="accountProfile" list={AccountProfileList} options={{ label: "내 계정" }} />
            ) : null}

            {canRead(p, "accountSessions") ? (
              <Resource name="accountSessions" list={AccountSessionList} options={{ label: "내 세션" }} />
            ) : null}
          </>
        );
      }}
    </Admin>
  );
}
