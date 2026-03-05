import { request } from "../../shared/api/http";
import type { AuditLog } from "../../types";

export const adminAuditApi = {
  auditLogs(token: string) {
    return request<AuditLog[]>("/api/admin/audit-logs", {}, token);
  },
};
