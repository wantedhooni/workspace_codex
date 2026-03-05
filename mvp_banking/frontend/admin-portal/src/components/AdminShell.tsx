import { Button, Card, Layout, Space, Typography } from "antd";
import { NavLink } from "react-router-dom";
import type { PropsWithChildren } from "react";
import type { Profile } from "../api";

type AdminShellProps = PropsWithChildren<{
  profile: Profile;
  unreadNotificationCount: number;
  onLogout: () => void;
}>;

const navItems = [
  { label: "Overview", to: "/" },
  { label: "Customers", to: "/customers" },
  { label: "Accounts", to: "/accounts" },
  { label: "Transactions", to: "/transactions" },
  { label: "FX Rates", to: "/fx-rates" },
  { label: "Linked Banks", to: "/linked-bank-accounts" },
  { label: "Funding", to: "/funding-requests" },
  { label: "Exchange", to: "/exchange-requests" },
  { label: "Stock Orders", to: "/stock-orders" },
  { label: "Stock Positions", to: "/stock-positions" },
  { label: "Approvals", to: "/approvals" },
  { label: "Announcements", to: "/announcements" },
  { label: "Notifications", to: "/notifications" },
  { label: "Audit Logs", to: "/audit-logs" },
];

export function AdminShell({ profile, unreadNotificationCount, onLogout, children }: AdminShellProps) {
  return (
    <Layout className="admin-layout">
      <Layout.Sider width={280} theme="light" className="admin-sider">
        <div className="brand-block">
          <p className="eyebrow">MVP Banking</p>
          <h1>Admin Portal</h1>
          <p className="muted">페이지 단위로 분리된 운영 콘솔</p>
        </div>
        <Space direction="vertical" size="middle" className="nav-block">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === "/"}
              className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}
            >
              <span>{item.label}</span>
              {item.to === "/notifications" && unreadNotificationCount > 0 ? (
                <span className="nav-badge">{unreadNotificationCount}</span>
              ) : null}
            </NavLink>
          ))}
        </Space>
        <Card className="operator-card" size="small">
          <Typography.Text strong>{profile.displayName}</Typography.Text>
          <Typography.Paragraph type="secondary" style={{ marginBottom: 12 }}>
            {profile.email}
          </Typography.Paragraph>
          <Typography.Paragraph type="secondary">{profile.roles.join(", ")}</Typography.Paragraph>
          <Button block onClick={onLogout}>
            로그아웃
          </Button>
        </Card>
      </Layout.Sider>
      <Layout.Content className="admin-content">{children}</Layout.Content>
    </Layout>
  );
}
