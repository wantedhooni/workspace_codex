import { request } from "../../shared/api/http";
import type { Approval } from "../../types";

export const adminApprovalsApi = {
  approvals(token: string) {
    return request<Approval[]>("/api/admin/approvals", {}, token);
  },
  approve(token: string, approvalId: string, reason: string) {
    return request<Approval>(
      `/api/admin/approvals/${approvalId}/approve`,
      {
        method: "POST",
        body: JSON.stringify({ reason }),
      },
      token,
    );
  },
  reject(token: string, approvalId: string, reason: string) {
    return request<Approval>(
      `/api/admin/approvals/${approvalId}/reject`,
      {
        method: "POST",
        body: JSON.stringify({ reason }),
      },
      token,
    );
  },
};
