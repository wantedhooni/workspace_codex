export type MetricTone = "positive" | "neutral" | "warning" | "critical";

export interface DashboardMetricCard {
  key: string;
  label: string;
  value: string;
  deltaLabel: string;
  deltaRate: number;
  tone: MetricTone;
}

export interface DashboardTrendPoint {
  label: string;
  value: number;
}

export interface DashboardBreakdownRow {
  segment: string;
  owner: string;
  status: string;
  orders: number;
  notional: string;
  pendingIssues: number;
}

export interface DashboardOverviewResponse {
  title: string;
  subtitle: string;
  assumptionNote: string;
  asOfDate: string;
  headlineMetrics: DashboardMetricCard[];
  trendPoints: DashboardTrendPoint[];
  breakdownRows: DashboardBreakdownRow[];
}
