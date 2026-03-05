import { request } from "../../shared/api/http";
import type { AdminOverview } from "../../types";

export const adminOverviewApi = {
  overview(token: string) {
    return request<AdminOverview>("/api/admin/overview", {}, token);
  },
};
