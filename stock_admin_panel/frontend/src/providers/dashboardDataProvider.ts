import type { BaseRecord, CustomParams, CustomResponse, DataProvider, HttpError } from "@refinedev/core";
import type { DashboardOverviewResponse } from "../types/dashboard";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api";

const fallbackDashboard: DashboardOverviewResponse = {
  title: "운영 대시보드",
  subtitle: "백엔드 연결 전에도 화면 구성을 검증할 수 있는 기본 데이터",
  assumptionNote:
    "가정: 승인/주문/리스크를 한 화면에서 확인하는 운영 대시보드이며, 현재는 프런트엔드 fallback 데이터가 활성화될 수 있습니다.",
  asOfDate: "2026-02-28",
  headlineMetrics: [
    { key: "aum", label: "총 운용 잔고", value: "₩128.4B", deltaLabel: "전일 대비 +2.4%", deltaRate: 2.4, tone: "positive" },
    { key: "orders", label: "금일 처리 주문", value: "1,284", deltaLabel: "목표 대비 91%", deltaRate: 91, tone: "neutral" },
    { key: "approvals", label: "대기 승인", value: "18", deltaLabel: "30분 초과 4건", deltaRate: -4, tone: "warning" },
    { key: "alerts", label: "오픈 알림", value: "6", deltaLabel: "치명도 높음 2건", deltaRate: -2, tone: "critical" },
  ],
  trendPoints: [
    { label: "02-22", value: 142 },
    { label: "02-23", value: 156 },
    { label: "02-24", value: 161 },
    { label: "02-25", value: 149 },
    { label: "02-26", value: 173 },
    { label: "02-27", value: 181 },
    { label: "02-28", value: 176 },
  ],
  breakdownRows: [
    { segment: "국내 주식", owner: "트레이딩 1팀", status: "안정", orders: 412, notional: "₩32.6B", pendingIssues: 1 },
    { segment: "해외 주식", owner: "글로벌 데스크", status: "주의", orders: 358, notional: "₩41.2B", pendingIssues: 3 },
    { segment: "ETF", owner: "패시브 운용", status: "안정", orders: 271, notional: "₩22.5B", pendingIssues: 0 },
    { segment: "파생/헤지", owner: "리스크 오퍼레이션", status: "점검", orders: 243, notional: "₩32.1B", pendingIssues: 2 },
  ],
};

async function requestOverview(): Promise<DashboardOverviewResponse> {
  try {
    const response = await fetch(`${API_URL}/dashboard/overview`);

    if (!response.ok) {
      throw new Error(`Dashboard API failed with status ${response.status}`);
    }

    return (await response.json()) as DashboardOverviewResponse;
  } catch (error) {
    console.warn("dashboard overview fallback activated", error);
    return fallbackDashboard;
  }
}

const notImplemented = async (): Promise<never> => {
  throw {
    message: "This data provider method is not implemented for the dashboard shell.",
    statusCode: 501,
  } as HttpError;
};

export const dashboardDataProvider: DataProvider = {
  getApiUrl: () => API_URL,
  getList: notImplemented,
  create: notImplemented,
  update: notImplemented,
  deleteOne: notImplemented,
  getOne: notImplemented,
  getMany: notImplemented,
  createMany: notImplemented,
  deleteMany: notImplemented,
  updateMany: notImplemented,
  custom: async <TData extends BaseRecord = BaseRecord>({
    url,
    method,
  }: CustomParams): Promise<CustomResponse<TData>> => {
    if (url === `${API_URL}/dashboard/overview` && (!method || method.toLowerCase() === "get")) {
      return {
        data: (await requestOverview()) as unknown as TData,
      };
    }

    throw {
      message: `Unsupported custom request: ${method ?? "get"} ${url}`,
      statusCode: 400,
    } as HttpError;
  },
};
