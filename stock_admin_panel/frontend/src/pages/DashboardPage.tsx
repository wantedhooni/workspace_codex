import { Alert, Card, Col, Row, Skeleton, Space, Table, Typography } from "antd";
import { useCustom } from "@refinedev/core";
import dayjs from "dayjs";
import { KpiCard } from "../components/KpiCard";
import { TrendChartCard } from "../components/TrendChartCard";
import type { DashboardBreakdownRow, DashboardMetricCard, DashboardOverviewResponse } from "../types/dashboard";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api";

const columns = [
  {
    title: "세그먼트",
    dataIndex: "segment",
    key: "segment",
  },
  {
    title: "담당 조직",
    dataIndex: "owner",
    key: "owner",
  },
  {
    title: "상태",
    dataIndex: "status",
    key: "status",
  },
  {
    title: "주문 수",
    dataIndex: "orders",
    key: "orders",
    align: "right" as const,
    render: (value: number) => value.toLocaleString("ko-KR"),
  },
  {
    title: "거래 규모",
    dataIndex: "notional",
    key: "notional",
    align: "right" as const,
  },
  {
    title: "미해결 이슈",
    dataIndex: "pendingIssues",
    key: "pendingIssues",
    align: "right" as const,
  },
];

export const DashboardPage = () => {
  const { result, query } = useCustom<DashboardOverviewResponse>({
    url: `${API_URL}/dashboard/overview`,
    method: "get",
  });

  const overview = result.data;

  if (query.isLoading || !overview) {
    return <Skeleton active paragraph={{ rows: 10 }} />;
  }

  return (
    <Space direction="vertical" size={24} style={{ width: "100%" }}>
      <Card bordered={false} className="dashboard-hero">
        <Typography.Text className="dashboard-hero__eyebrow">Admin Console</Typography.Text>
        <Typography.Title level={2} style={{ marginTop: 12, marginBottom: 8 }}>
          {overview.title}
        </Typography.Title>
        <Typography.Paragraph type="secondary" style={{ maxWidth: 720, marginBottom: 16 }}>
          {overview.subtitle}
        </Typography.Paragraph>
        <Alert
          type="info"
          showIcon
          message={overview.assumptionNote}
          description={`기준일: ${dayjs(overview.asOfDate).format("YYYY년 MM월 DD일")}`}
        />
      </Card>

      <Row gutter={[16, 16]}>
        {overview.headlineMetrics.map((metric: DashboardMetricCard) => (
          <Col xs={24} sm={12} xl={6} key={metric.key}>
            <KpiCard metric={metric} />
          </Col>
        ))}
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={15}>
          <TrendChartCard points={overview.trendPoints} />
        </Col>
        <Col xs={24} xl={9}>
          <Card title="운영 메모" bordered={false}>
            <Space direction="vertical" size={12}>
              <Typography.Text>실데이터 연결 포인트: `GET /api/dashboard/overview`</Typography.Text>
              <Typography.Text>확장 방향: 계좌/브로커/리스크 룰별 drill-down 카드 추가</Typography.Text>
              <Typography.Text>주의: 현재 지표 정의가 없으므로 KPI 명칭은 임시 가정값입니다.</Typography.Text>
            </Space>
          </Card>
        </Col>
      </Row>

      <Card
        title="세그먼트별 현황"
        extra={<Typography.Text type="secondary">headline KPI를 뒷받침하는 기본 분해 테이블</Typography.Text>}
        bordered={false}
      >
        <Table<DashboardBreakdownRow>
          rowKey={(record) => `${record.segment}-${record.owner}`}
          columns={columns}
          dataSource={overview.breakdownRows}
          pagination={false}
        />
      </Card>
    </Space>
  );
};
