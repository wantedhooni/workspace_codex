import { ArrowDownOutlined, ArrowUpOutlined, MinusOutlined } from "@ant-design/icons";
import { Card, Space, Tag, Typography } from "antd";
import type { DashboardMetricCard } from "../types/dashboard";

const toneColorMap: Record<DashboardMetricCard["tone"], string> = {
  positive: "green",
  neutral: "default",
  warning: "gold",
  critical: "red",
};

const ToneIcon = ({ deltaRate }: { deltaRate: number }) => {
  if (deltaRate > 0) {
    return <ArrowUpOutlined />;
  }

  if (deltaRate < 0) {
    return <ArrowDownOutlined />;
  }

  return <MinusOutlined />;
};

export const KpiCard = ({ metric }: { metric: DashboardMetricCard }) => {
  return (
    <Card className="kpi-card" bordered={false}>
      <Typography.Text type="secondary">{metric.label}</Typography.Text>
      <Typography.Title level={3} style={{ marginTop: 12, marginBottom: 16 }}>
        {metric.value}
      </Typography.Title>
      <Space align="center">
        <Tag color={toneColorMap[metric.tone]} icon={<ToneIcon deltaRate={metric.deltaRate} />}>
          {metric.deltaLabel}
        </Tag>
      </Space>
    </Card>
  );
};
