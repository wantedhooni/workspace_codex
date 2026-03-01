import { Card, Space, Typography } from "antd";
import type { DashboardTrendPoint } from "../types/dashboard";

function buildTrendPath(points: DashboardTrendPoint[], width: number, height: number) {
  const maxValue = Math.max(...points.map((point) => point.value));
  const minValue = Math.min(...points.map((point) => point.value));
  const range = Math.max(maxValue - minValue, 1);

  return points
    .map((point, index) => {
      const x = (index / Math.max(points.length - 1, 1)) * width;
      const y = height - ((point.value - minValue) / range) * height;
      return `${index === 0 ? "M" : "L"} ${x.toFixed(1)} ${y.toFixed(1)}`;
    })
    .join(" ");
}

export const TrendChartCard = ({ points }: { points: DashboardTrendPoint[] }) => {
  const width = 640;
  const height = 220;
  const trendPath = buildTrendPath(points, width, height);

  return (
    <Card
      title="주문 추이"
      extra={<Typography.Text type="secondary">최근 7일 처리량</Typography.Text>}
      bordered={false}
    >
      <div className="trend-chart">
        <svg viewBox={`0 0 ${width} ${height + 30}`} role="img" aria-label="최근 7일 주문 추이">
          <defs>
            <linearGradient id="trendFill" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#86efac" stopOpacity="0.45" />
              <stop offset="100%" stopColor="#f0fdf4" stopOpacity="0.05" />
            </linearGradient>
          </defs>
          <path d={`${trendPath} L ${width} ${height} L 0 ${height} Z`} fill="url(#trendFill)" />
          <path d={trendPath} fill="none" stroke="#166534" strokeWidth="4" strokeLinecap="round" />
          {points.map((point, index) => {
            const x = (index / Math.max(points.length - 1, 1)) * width;
            const maxValue = Math.max(...points.map((currentPoint) => currentPoint.value));
            const minValue = Math.min(...points.map((currentPoint) => currentPoint.value));
            const y = height - ((point.value - minValue) / Math.max(maxValue - minValue, 1)) * height;

            return (
              <g key={point.label}>
                <circle cx={x} cy={y} r="5" fill="#14532d" />
                <text x={x} y={height + 20} textAnchor="middle" className="trend-chart__label">
                  {point.label}
                </text>
              </g>
            );
          })}
        </svg>
        <Space className="trend-chart__legend" size={24}>
          {points.map((point) => (
            <Space key={point.label} size={8}>
              <span className="trend-chart__dot" />
              <Typography.Text>{`${point.label} · ${point.value}`}</Typography.Text>
            </Space>
          ))}
        </Space>
      </div>
    </Card>
  );
};
