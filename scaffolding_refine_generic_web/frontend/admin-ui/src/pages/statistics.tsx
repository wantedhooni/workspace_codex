import { List } from "@refinedev/antd";
import { Alert, Button, Card, Col, Row, Space, Spin, Table, Typography } from "antd";
import { useCallback, useEffect, useMemo, useState } from "react";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8090";

type Overview = {
  totalAdmins: number;
  totalRoles: number;
  totalPermissions: number;
  totalMenus: number;
  totalPrograms: number;
  totalContents: number;
  totalAccessLogs: number;
  totalServiceAuditLogs: number;
  todayRequests: number;
  todaySuccessfulLogins: number;
};

type PathUsage = { path: string; total: number };
type ActionUsage = { action: string; total: number };

type Dashboard = {
  overview: Overview;
  topPaths: PathUsage[];
  topActions: ActionUsage[];
};

const defaultOverview: Overview = {
  totalAdmins: 0,
  totalRoles: 0,
  totalPermissions: 0,
  totalMenus: 0,
  totalPrograms: 0,
  totalContents: 0,
  totalAccessLogs: 0,
  totalServiceAuditLogs: 0,
  todayRequests: 0,
  todaySuccessfulLogins: 0,
};

export const StatisticsPage = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>("");
  const [loadedAt, setLoadedAt] = useState<string>("");
  const [dashboard, setDashboard] = useState<Dashboard>({
    overview: defaultOverview,
    topPaths: [],
    topActions: [],
  });

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const response = await fetch(`${API_URL}/statistics/dashboard?limit=10`, {
        credentials: "include",
      });
      if (!response.ok) {
        throw new Error(await response.text());
      }
      const data = (await response.json()) as Dashboard;
      setDashboard(data);
      setLoadedAt(new Date().toLocaleString());
    } catch (e) {
      setError(e instanceof Error ? e.message : "failed to load statistics");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  const topCards = useMemo(
    () => [
      { title: "Admins", value: dashboard.overview.totalAdmins },
      { title: "Roles", value: dashboard.overview.totalRoles },
      { title: "Permissions", value: dashboard.overview.totalPermissions },
      { title: "Menus", value: dashboard.overview.totalMenus },
      { title: "Programs", value: dashboard.overview.totalPrograms },
      { title: "Contents", value: dashboard.overview.totalContents },
      { title: "Today Requests", value: dashboard.overview.todayRequests },
      { title: "Today Logins", value: dashboard.overview.todaySuccessfulLogins },
      { title: "Access Logs", value: dashboard.overview.totalAccessLogs },
      { title: "Audit Logs", value: dashboard.overview.totalServiceAuditLogs },
    ],
    [dashboard],
  );

  return (
    <List
      title="Statistics Dashboard"
      headerButtons={
        <Space>
          <Typography.Text type="secondary">Updated: {loadedAt || "-"}</Typography.Text>
          <Button onClick={loadDashboard} loading={loading}>
            Refresh
          </Button>
        </Space>
      }
    >
      {loading ? (
        <Spin />
      ) : (
        <>
          {error ? <Alert type="error" message={error} /> : null}

          <Row gutter={[12, 12]}>
            {topCards.map((item) => (
              <Col key={item.title} xs={24} sm={12} md={8} lg={6}>
                <Card size="small" title={item.title}>
                  <Typography.Title level={4} style={{ margin: 0 }}>
                    {item.value}
                  </Typography.Title>
                </Card>
              </Col>
            ))}
          </Row>

          <Row gutter={[12, 12]} style={{ marginTop: 12 }}>
            <Col xs={24} lg={12}>
              <Card title="Top Paths" size="small">
                <Table<PathUsage> size="small" rowKey="path" dataSource={dashboard.topPaths} pagination={false}>
                  <Table.Column<PathUsage> dataIndex="path" title="Path" />
                  <Table.Column<PathUsage> dataIndex="total" title="Count" />
                </Table>
              </Card>
            </Col>
            <Col xs={24} lg={12}>
              <Card title="Top Actions" size="small">
                <Table<ActionUsage> size="small" rowKey="action" dataSource={dashboard.topActions} pagination={false}>
                  <Table.Column<ActionUsage> dataIndex="action" title="Action" />
                  <Table.Column<ActionUsage> dataIndex="total" title="Count" />
                </Table>
              </Card>
            </Col>
          </Row>
        </>
      )}
    </List>
  );
};
