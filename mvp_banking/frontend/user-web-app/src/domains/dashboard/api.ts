import { request } from "../../shared/api/http";
import type { DashboardInsight } from "./types";

export const dashboardApi = {
  insights(token: string) {
    return request<DashboardInsight>("/api/user/dashboard/insights", {}, token);
  },
};
