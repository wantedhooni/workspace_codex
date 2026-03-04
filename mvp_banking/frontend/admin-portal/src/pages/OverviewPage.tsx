import { Alert, Card, Col, Descriptions, List, Row, Statistic, Table, Tag, Typography } from "antd";
import type { AdminOverview, Announcement, Approval, AuditLog, FundingRequest } from "../api";

type OverviewPageProps = {
  overview: AdminOverview;
  fundingRequests: FundingRequest[];
  announcements: Announcement[];
  approvals: Approval[];
  auditLogs: AuditLog[];
};

export function OverviewPage({ overview, fundingRequests, announcements, approvals, auditLogs }: OverviewPageProps) {
  const visibleAnnouncements = announcements.filter((item) => item.status === "PUBLISHED").slice(0, 4);
  const recentFundingRequests = fundingRequests.slice(0, 5);

  return (
    <>
      <div>
        <Typography.Title level={2} style={{ marginBottom: 8 }}>
          Operations Overview
        </Typography.Title>
        <Typography.Paragraph type="secondary" style={{ margin: 0 }}>
          운영 대기열, 시세 freshness, 고객 심사 상태, 입출금 요청 볼륨을 한 화면에서 확인할 수 있는 실무형 운영 요약입니다.
        </Typography.Paragraph>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={12} xl={6}>
          <Card>
            <Statistic title="Pending Approvals" value={overview.metrics.pendingApprovals} suffix="건" />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card>
            <Statistic title="Overdue Approvals" value={overview.metrics.overdueApprovals} suffix="건" valueStyle={{ color: overview.metrics.overdueApprovals > 0 ? "#cf1322" : undefined }} />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card>
            <Statistic title="Review Required Customers" value={overview.metrics.reviewRequiredCustomers} suffix="명" valueStyle={{ color: overview.metrics.reviewRequiredCustomers > 0 ? "#d46b08" : undefined }} />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card>
            <Statistic title="Locked Accounts" value={overview.metrics.lockedAccounts} suffix="개" valueStyle={{ color: overview.metrics.lockedAccounts > 0 ? "#d46b08" : undefined }} />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card>
            <Statistic title="Pending Funding" value={overview.metrics.pendingFundingRequests} suffix="건" valueStyle={{ color: overview.metrics.pendingFundingRequests > 0 ? "#d46b08" : undefined }} />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card>
            <Statistic title="Pending Exchanges" value={overview.metrics.pendingExchanges} suffix="건" />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card>
            <Statistic title="Pending Stock Orders" value={overview.metrics.pendingStockOrders} suffix="건" />
          </Card>
        </Col>
        <Col xs={24} md={12} xl={6}>
          <Card>
            <Statistic title="Partial Fill Orders" value={overview.metrics.partiallyFilledOrders} suffix="건" />
          </Card>
        </Col>
        <Col xs={24}>
          <Card>
            <Statistic title="Pending Instruction Volume" value={Number(overview.metrics.pendingInstructionVolumeKrw).toLocaleString()} suffix="KRW" />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 8 }}>
        <Col xs={24} xl={14}>
          <Card title="Operations Alerts">
            <List
              dataSource={overview.alerts}
              renderItem={(item) => (
                <List.Item>
                  <Alert
                    type={resolveAlertType(item.severity)}
                    showIcon
                    message={item.title}
                    description={`${item.message} ${item.actionPath !== "/" ? `검토 화면: ${item.actionPath}` : ""}`.trim()}
                    style={{ width: "100%" }}
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>
        <Col xs={24} xl={10}>
          <Card title="Market Data Status">
            <Descriptions column={1} size="small" labelStyle={{ width: 180 }}>
              <Descriptions.Item label="Latest FX Rate">
                {overview.marketStatus.latestFxEffectiveAt ? new Date(overview.marketStatus.latestFxEffectiveAt).toLocaleString() : "-"}
              </Descriptions.Item>
              <Descriptions.Item label="Latest Stock Quote">
                {overview.marketStatus.latestStockQuoteEffectiveAt ? new Date(overview.marketStatus.latestStockQuoteEffectiveAt).toLocaleString() : "-"}
              </Descriptions.Item>
              <Descriptions.Item label="FX Freshness">
                <Tag color={overview.marketStatus.staleFxPairs > 0 ? "red" : "green"}>
                  {`fresh ${overview.marketStatus.freshFxPairs} / stale ${overview.marketStatus.staleFxPairs}`}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Stock Quote Freshness">
                <Tag color={overview.marketStatus.staleStockQuotes > 0 ? "red" : "green"}>
                  {`fresh ${overview.marketStatus.freshStockQuotes} / stale ${overview.marketStatus.staleStockQuotes}`}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Recent Audit Events">
                {auditLogs.length}건
              </Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 8 }}>
        <Col xs={24} xl={12}>
          <Card title="Pending Approval Snapshot">
            <Table
              rowKey="id"
              dataSource={approvals.slice(0, 5)}
              pagination={false}
              columns={[
                { title: "Title", dataIndex: "title" },
                { title: "Target", dataIndex: "targetType" },
                {
                  title: "Status",
                  dataIndex: "status",
                  render: (value: string) => (
                    <Tag color={value === "PENDING" ? "gold" : value === "APPROVED" ? "green" : "red"}>{value}</Tag>
                  ),
                },
              ]}
            />
          </Card>
        </Col>
        <Col xs={24} xl={12}>
          <Card title="Recent Funding Requests">
            <Table
              rowKey="id"
              dataSource={recentFundingRequests}
              pagination={false}
              locale={{ emptyText: "입출금 요청이 없습니다." }}
              columns={[
                {
                  title: "Type",
                  width: 120,
                  render: (_, record: FundingRequest) => (
                    <Tag color={record.requestType === "WITHDRAWAL" ? "volcano" : "green"}>{record.requestType}</Tag>
                  ),
                },
                {
                  title: "Request",
                  render: (_, record: FundingRequest) => (
                    <div>
                      <Typography.Text strong>{record.requestNumber}</Typography.Text>
                      <div>
                        <Typography.Text type="secondary">{record.customerEmail}</Typography.Text>
                      </div>
                    </div>
                  ),
                },
                {
                  title: "Account",
                  render: (_, record: FundingRequest) => `${record.accountNumber} / ${record.accountType}`,
                },
                {
                  title: "Status",
                  render: (_, record: FundingRequest) => (
                    <Tag color={record.status === "PENDING_APPROVAL" ? "gold" : record.status === "APPROVED" ? "green" : "red"}>
                      {record.status}
                    </Tag>
                  ),
                },
              ]}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 8 }}>
        <Col xs={24} xl={12}>
          <Card title="Recent Audit Activity">
            <Table
              rowKey="id"
              dataSource={auditLogs.slice(0, 5)}
              pagination={false}
              columns={[
                { title: "Actor", dataIndex: "actorEmail" },
                { title: "Action", dataIndex: "actionType" },
                { title: "At", render: (_, record: AuditLog) => new Date(record.loggedAt).toLocaleString() },
              ]}
            />
          </Card>
        </Col>
        <Col xs={24} xl={12}>
          <Card title="Service Announcements">
            <Table
              rowKey="id"
              dataSource={visibleAnnouncements}
              pagination={false}
              locale={{ emptyText: "현재 게시 중인 공지가 없습니다." }}
              columns={[
                {
                  title: "Priority",
                  width: 120,
                  render: (_, record: Announcement) => (
                    <Tag color={record.severity === "CRITICAL" ? "red" : record.severity === "WARNING" ? "gold" : "blue"}>
                      {record.severity}
                    </Tag>
                  ),
                },
                { title: "Title", dataIndex: "title" },
                { title: "Audience", dataIndex: "audience", width: 120 },
                {
                  title: "Window",
                  width: 280,
                  render: (_, record: Announcement) => (
                    <div>
                      <div>{record.startsAt ? new Date(record.startsAt).toLocaleString() : "즉시 노출"}</div>
                      <Typography.Text type="secondary">
                        {record.endsAt ? `~ ${new Date(record.endsAt).toLocaleString()}` : "~ 종료 시까지"}
                      </Typography.Text>
                    </div>
                  ),
                },
              ]}
            />
          </Card>
        </Col>
      </Row>
    </>
  );
}

function resolveAlertType(severity: string): "success" | "info" | "warning" | "error" {
  if (severity === "HIGH") {
    return "error";
  }
  if (severity === "MEDIUM" || severity === "WATCH") {
    return "warning";
  }
  if (severity === "STABLE") {
    return "success";
  }
  return "info";
}
