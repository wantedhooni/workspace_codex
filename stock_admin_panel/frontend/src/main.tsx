import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Refine } from "@refinedev/core";
import { ConfigProvider, Layout, Menu, Typography } from "antd";
import koKR from "antd/locale/ko_KR";
import { DashboardPage } from "./pages/DashboardPage";
import { dashboardDataProvider } from "./providers/dashboardDataProvider";
import "./styles/global.css";

const queryClient = new QueryClient();

const AppShell = () => {
  return (
    <Layout className="app-shell">
      <Layout.Sider breakpoint="lg" collapsedWidth="0" width={240} theme="light">
        <div className="app-shell__brand">
          <Typography.Text strong>Stock Admin</Typography.Text>
          <Typography.Text type="secondary">Dashboard Shell</Typography.Text>
        </div>
        <Menu
          mode="inline"
          selectedKeys={["dashboard"]}
          items={[
            {
              key: "dashboard",
              label: "운영 대시보드",
            },
          ]}
        />
      </Layout.Sider>
      <Layout>
        <Layout.Header className="app-shell__header">
          <Typography.Title level={4} style={{ margin: 0 }}>
            Dashboard
          </Typography.Title>
        </Layout.Header>
        <Layout.Content>
          <DashboardPage />
        </Layout.Content>
      </Layout>
    </Layout>
  );
};

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <ConfigProvider
      locale={koKR}
      theme={{
        token: {
          colorPrimary: "#14532d",
          colorInfo: "#14532d",
          borderRadius: 16,
        },
      }}
    >
      <BrowserRouter>
        <QueryClientProvider client={queryClient}>
          <Refine
            dataProvider={dashboardDataProvider}
            resources={[
              {
                name: "dashboard",
                list: "/dashboard",
              },
            ]}
          >
            <Routes>
              <Route path="/dashboard" element={<AppShell />} />
              <Route path="*" element={<Navigate to="/dashboard" replace />} />
            </Routes>
          </Refine>
        </QueryClientProvider>
      </BrowserRouter>
    </ConfigProvider>
  </React.StrictMode>,
);
