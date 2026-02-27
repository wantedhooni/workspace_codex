import type {
  CreateParams,
  CreateResult,
  DataProvider,
  DeleteParams,
  DeleteResult,
  GetListParams,
  GetListResult,
  Identifier,
  RaRecord,
  UpdateParams,
  UpdateResult
} from "react-admin";
import { getAuthHeaders } from "./authProvider";
import { getSelectedPortfolioId } from "../portfolio/portfolioSelection";

const apiUrl = "http://127.0.0.1:8088";

type Row = RaRecord & Record<string, unknown>;

type ApiError = {
  code?: string;
  message?: string;
  details?: unknown;
};

function toList(items: Row[]): GetListResult<Row> {
  return {
      data: items.map((it, idx) => ({
        ...it,
        id:
          (it.id as Identifier) ??
          (it.historyId as Identifier) ??
          (it.alertKey as Identifier) ??
          (it.healthKey as Identifier) ??
          (it.qualityKey as Identifier) ??
          (it.auditId as Identifier) ??
        (it.orderId as Identifier) ??
        (it.tradeId as Identifier) ??
        (it.voucherId as Identifier) ??
        (it.ledgerEntryId as Identifier) ??
        (it.portfolioId as Identifier) ??
        (it.userId as Identifier) ??
        (it.roleId as Identifier) ??
        (it.menuId as Identifier) ??
        (it.menuPermissionId as Identifier) ??
        (it.sessionId as Identifier) ??
        idx + 1
    })),
    total: items.length
  };
}

function queryString(filter?: Record<string, unknown>) {
  const query = new URLSearchParams();
  Object.entries(filter ?? {}).forEach(([k, v]) => {
    if (v !== undefined && v !== null && String(v).trim() !== "") {
      query.set(k, String(v));
    }
  });
  return query.toString();
}

function normalizePortfolioFilter(filter: Record<string, unknown> | undefined) {
  const next = { ...(filter ?? {}) };
  const parsed = Number(next.portfolioId ?? 0);
  if (!Number.isFinite(parsed) || parsed <= 0) {
    next.portfolioId = getSelectedPortfolioId(1);
  } else {
    next.portfolioId = Math.trunc(parsed);
  }
  return next;
}

async function readApiError(res: Response): Promise<string> {
  try {
    const body = (await res.json()) as ApiError;
    if (body?.message) {
      if (typeof body.details === "string" && body.details.trim() !== "") {
        return `${body.message} (${body.details})`;
      }
      if (Array.isArray(body.details) && body.details.length > 0) {
        const first = body.details[0] as Record<string, unknown>;
        const field = typeof first?.field === "string" ? first.field : "";
        const detailMessage = typeof first?.message === "string" ? first.message : "";
        if (field || detailMessage) {
          return `${body.message} (${field}${field && detailMessage ? ": " : ""}${detailMessage})`;
        }
      }
      return body.message;
    }
  } catch {
    // ignore parse failure
  }
  return `${res.status} ${res.statusText}`;
}

function buildHeaders(extra?: Record<string, string>) {
  return {
    ...getAuthHeaders(),
    ...(extra ?? {})
  };
}

async function getJson(url: string) {
  const res = await fetch(url, { headers: buildHeaders() });
  if (!res.ok) {
    throw new Error(await readApiError(res));
  }
  return res.json();
}

async function postJson(url: string, body: unknown) {
  const res = await fetch(url, {
    method: "POST",
    headers: buildHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify(body)
  });
  if (!res.ok) {
    throw new Error(await readApiError(res));
  }
  return res.json();
}

async function putJson(url: string, body: unknown) {
  const res = await fetch(url, {
    method: "PUT",
    headers: buildHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify(body)
  });
  if (!res.ok) {
    throw new Error(await readApiError(res));
  }
  return res.json();
}

async function deleteJson(url: string) {
  const res = await fetch(url, {
    method: "DELETE",
    headers: buildHeaders()
  });
  if (!res.ok) {
    throw new Error(await readApiError(res));
  }
  return res.json();
}

function normalizeBoolean(value: unknown): boolean {
  if (typeof value === "boolean") return value;
  if (typeof value === "string") {
    return value.toLowerCase() === "true";
  }
  return Boolean(value);
}

function normalizeOptionalNumber(value: unknown): number | null {
  if (value === undefined || value === null || value === "") {
    return null;
  }
  const parsed = Number(value);
  if (Number.isNaN(parsed)) {
    return null;
  }
  return parsed;
}

function normalizeUserCreatePayload(data: Record<string, unknown>) {
  const rawRoleCodes = data.roleCodes;
  const roleCodes = Array.isArray(rawRoleCodes)
    ? rawRoleCodes.map((code) => String(code).trim()).filter(Boolean)
    : String(rawRoleCodes ?? "")
        .split(",")
        .map((code) => code.trim())
        .filter(Boolean);

  return {
    email: String(data.email ?? ""),
    name: String(data.name ?? ""),
    status: String(data.status ?? "ACTIVE"),
    roleCodes
  };
}

function normalizeRoleCreatePayload(data: Record<string, unknown>) {
  return {
    roleCode: String(data.roleCode ?? ""),
    roleName: String(data.roleName ?? ""),
    description: String(data.description ?? ""),
    systemRole: normalizeBoolean(data.systemRole)
  };
}

function normalizeMenuCreatePayload(data: Record<string, unknown>) {
  return {
    parentMenuId: normalizeOptionalNumber(data.parentMenuId),
    menuKey: String(data.menuKey ?? ""),
    menuLabel: String(data.menuLabel ?? ""),
    path: String(data.path ?? ""),
    icon: String(data.icon ?? "view_list"),
    sortOrder: Number(data.sortOrder ?? 999),
    enabled: normalizeBoolean(data.enabled)
  };
}

function normalizeMenuPermissionPayload(data: Record<string, unknown>) {
  return {
    menuId: Number(data.menuId),
    roleId: Number(data.roleId),
    canRead: normalizeBoolean(data.canRead),
    canCreate: normalizeBoolean(data.canCreate),
    canUpdate: normalizeBoolean(data.canUpdate),
    canDelete: normalizeBoolean(data.canDelete)
  };
}

function normalizeSavedViewPayload(data: Record<string, unknown>) {
  const filtersInput = data.filters as Record<string, unknown> | undefined;
  const filters: Record<string, string> = {};
  Object.entries(filtersInput ?? {}).forEach(([key, value]) => {
    if (value === null || value === undefined) {
      return;
    }
    const text = String(value).trim();
    if (!text) {
      return;
    }
    filters[key] = text;
  });

  return {
    resourceKey: String(data.resourceKey ?? ""),
    viewName: String(data.viewName ?? ""),
    description: String(data.description ?? ""),
    shared: normalizeBoolean(data.shared ?? true),
    filters
  };
}

export const dataProvider: DataProvider = {
  async getList(resource: string, params: GetListParams): Promise<GetListResult<Row>> {
    const rawFilter = (params.filter ?? {}) as Record<string, unknown>;
    const portfolioScopedResources = new Set([
      "orders",
      "orderAuditSummaries",
      "orderAudits",
      "trades",
      "positions",
      "riskLimits",
      "tradingControlHistory",
      "riskAlerts",
      "riskAlertOverviews",
      "orderHealth",
      "executionQualities",
      "portfolioSummaries",
      "portfolioSummaryInsights",
      "portfolioProfitPlaybook",
      "portfolioProfitPlaybookFeedback",
      "journalVouchers",
      "ledgerEntries"
    ]);
    const filter = portfolioScopedResources.has(resource)
      ? normalizePortfolioFilter(rawFilter)
      : rawFilter;

    if (resource === "orderWorkbench") {
      const normalized = normalizePortfolioFilter(rawFilter);
      const qs = queryString(normalized);
      const json = await getJson(`${apiUrl}/api/orders/workbench${qs ? `?${qs}` : ""}`);
      return {
        data: [
          {
            id: String(json.summary?.portfolioId ?? normalized.portfolioId ?? 1),
            ...json
          }
        ],
        total: 1
      };
    }

    if (resource === "orders") {
      const qs = queryString(filter);
      const json = await getJson(`${apiUrl}/api/orders${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "orderAudits") {
      const qs = queryString(filter);
      const json = await getJson(`${apiUrl}/api/orders/audit-logs${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "orderAuditSummaries") {
      const qs = queryString(filter);
      const json = await getJson(`${apiUrl}/api/orders/audit-logs/summary${qs ? `?${qs}` : ""}`);
      const row = json as Row;
      const fallbackPortfolioId = Number(filter.portfolioId ?? getSelectedPortfolioId(1));
      return {
        data: [
          {
            ...row,
            id: row.id ?? `order-audit-summary-${fallbackPortfolioId}-${String(row.generatedAt ?? Date.now())}`
          }
        ],
        total: 1
      };
    }

    if (resource === "trades") {
      const qs = queryString(filter);
      const json = await getJson(`${apiUrl}/api/trades${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "positions") {
      const portfolioId = Number(filter.portfolioId ?? getSelectedPortfolioId(1));
      const json = await getJson(`${apiUrl}/api/positions?portfolioId=${portfolioId}`);
      return toList(json.items ?? []);
    }

    if (resource === "riskLimits") {
      const qs = queryString(filter);
      const json = await getJson(`${apiUrl}/api/risk-limits${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "tradingControlHistory") {
      const qs = queryString(filter);
      const json = await getJson(`${apiUrl}/api/risk-limits/trading-controls/history${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "riskAlerts") {
      const qs = queryString(filter);
      const json = await getJson(`${apiUrl}/api/risk-alerts${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "riskAlertOverviews") {
      const qs = queryString(filter);
      const json = await getJson(`${apiUrl}/api/risk-alerts/overview${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "orderHealth") {
      const qs = queryString(filter);
      const json = await getJson(`${apiUrl}/api/order-health${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "executionQualities") {
      const qs = queryString(filter);
      const json = await getJson(`${apiUrl}/api/execution-qualities${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "portfolioSummaries") {
      const qs = queryString(filter);
      const json = await getJson(`${apiUrl}/api/portfolio-summaries${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "portfolioSummaryInsights") {
      const qs = queryString(filter);
      const json = await getJson(`${apiUrl}/api/portfolio-summaries/insight${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "portfolioProfitPlaybook") {
      const qs = queryString(filter);
      const json = await getJson(`${apiUrl}/api/portfolio-summaries/profit-playbook${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "portfolioProfitPlaybookFeedback") {
      const qs = queryString(filter);
      const json = await getJson(`${apiUrl}/api/portfolio-summaries/profit-playbook/feedback${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "portfolios") {
      const qs = queryString(filter);
      const json = await getJson(`${apiUrl}/api/portfolios${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "globalSearch") {
      const qs = queryString(rawFilter);
      const json = await getJson(`${apiUrl}/api/search/global${qs ? `?${qs}` : ""}`);
      return {
        data: [
          {
            id: "global-search",
            ...json
          }
        ],
        total: 1
      };
    }

    if (resource === "savedViews") {
      const qs = queryString(rawFilter);
      const json = await getJson(`${apiUrl}/api/saved-views${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "savedViewDefaults") {
      const resourceKey = String(rawFilter.resourceKey ?? "");
      if (!resourceKey) {
        return { data: [], total: 0 };
      }
      const json = await getJson(`${apiUrl}/api/saved-views/default?resourceKey=${encodeURIComponent(resourceKey)}`);
      const viewId = Number(json.viewId ?? 0);
      if (!Number.isFinite(viewId) || viewId <= 0) {
        return { data: [], total: 0 };
      }
      return {
        data: [
          {
            ...json,
            id: `${resourceKey}-${viewId}`
          }
        ],
        total: 1
      };
    }

    if (resource === "users") {
      const qs = queryString((params.filter ?? {}) as Record<string, unknown>);
      const json = await getJson(`${apiUrl}/api/users${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "roles") {
      const qs = queryString((params.filter ?? {}) as Record<string, unknown>);
      const json = await getJson(`${apiUrl}/api/roles${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "menus") {
      const qs = queryString((params.filter ?? {}) as Record<string, unknown>);
      const json = await getJson(`${apiUrl}/api/menus${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "menuPermissions") {
      const qs = queryString((params.filter ?? {}) as Record<string, unknown>);
      const json = await getJson(`${apiUrl}/api/menu-permissions${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "accountProfile") {
      const json = await getJson(`${apiUrl}/api/account/me`);
      return toList([json]);
    }

    if (resource === "accountSessions") {
      const json = await getJson(`${apiUrl}/api/account/sessions`);
      return toList(json.items ?? []);
    }

    if (resource === "accountWorkQueue") {
      const qs = queryString(rawFilter);
      const json = await getJson(`${apiUrl}/api/account/work-queue${qs ? `?${qs}` : ""}`);
      const id = `work-queue-${Number(json?.summary?.portfolioId ?? rawFilter?.portfolioId ?? 1)}`;
      return {
        data: [{ ...json, id }],
        total: 1
      };
    }

    if (resource === "accountActivityFeed") {
      const qs = queryString(rawFilter);
      const json = await getJson(`${apiUrl}/api/account/activity-feed${qs ? `?${qs}` : ""}`);
      const items = Array.isArray(json.items) ? json.items : [];
      return toList(items);
    }

    if (resource === "journalVouchers") {
      const qs = queryString(filter);
      const json = await getJson(`${apiUrl}/api/journal-vouchers${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    if (resource === "ledgerEntries") {
      const qs = queryString(filter);
      const json = await getJson(`${apiUrl}/api/ledgers/entries${qs ? `?${qs}` : ""}`);
      return toList(json.items ?? []);
    }

    return { data: [], total: 0 };
  },

  async getOne(resource: string, params: { id: Identifier }) {
    if (resource === "orderInsights") {
      const staleMinutes = Number((params as { meta?: { staleMinutes?: number } }).meta?.staleMinutes ?? 30);
      const qs = Number.isFinite(staleMinutes) ? `?staleMinutes=${Math.max(0, Math.trunc(staleMinutes))}` : "";
      const json = await getJson(`${apiUrl}/api/orders/${params.id}/insight${qs}`);
      return { data: { ...json, id: json.orderId ?? params.id } };
    }

    return { data: { id: params.id } };
  },

  async getMany() {
    return { data: [] };
  },

  async getManyReference() {
    return { data: [], total: 0 };
  },

  async update(resource: string, params: UpdateParams): Promise<UpdateResult<Row>> {
    if (resource === "orders") {
      const action = (params.meta as Record<string, unknown> | undefined)?.action as string | undefined;
      const reason = String((params.data as Record<string, unknown> | undefined)?.reason ?? "").trim();

      if (!action || !["cancel", "reject"].includes(action)) {
        throw new Error("orders update requires meta.action in [cancel, reject]");
      }

      const json = await postJson(`${apiUrl}/api/orders/${params.id}/${action}`, {
        reason
      });

      return {
        data: {
          ...(params.previousData as Row),
          ...json,
          status: json.status,
          decisionReason: json.reason,
          decidedAt: json.decidedAt,
          id: params.id
        }
      };
    }

    if (resource === "journalVouchers") {
      const action = (params.meta as Record<string, unknown> | undefined)?.action as string | undefined;
      if (!action || !["approve", "post", "cancel"].includes(action)) {
        throw new Error("journalVouchers update requires meta.action in [approve, post, cancel]");
      }
      const json = await postJson(`${apiUrl}/api/journal-vouchers/${params.id}/${action}`, {});
      return { data: { ...(params.previousData as Row), ...json, id: json.voucherId } };
    }

    if (resource === "riskLimits") {
      const action = (params.meta as Record<string, unknown> | undefined)?.action as string | undefined;
      if (action === "toggleTrading") {
        const payload = params.data as Record<string, unknown>;
        const json = await putJson(`${apiUrl}/api/risk-limits/trading-controls`, {
          portfolioId: Number(payload.portfolioId ?? params.id),
          tradingEnabled: normalizeBoolean(payload.tradingEnabled),
          reason: String(payload.reason ?? "")
        });
        const row = (json.items?.[0] ?? params.data) as Row;
        return {
          data: {
            ...(params.previousData as Row),
            ...row,
            id: row.portfolioId as Identifier
          }
        };
      }

      const json = await putJson(`${apiUrl}/api/risk-limits`, params.data);
      const row = (json.items?.[0] ?? params.data) as Row;
      return { data: { ...row, id: row.portfolioId as Identifier } };
    }

    if (resource === "riskAlerts") {
      const action = (params.meta as Record<string, unknown> | undefined)?.action as string | undefined;
      const payload = params.data as Record<string, unknown>;
      const portfolioId = Number(payload.portfolioId ?? 0);
      const alertKey = String(payload.alertKey ?? params.id ?? "");

      if (!Number.isFinite(portfolioId) || portfolioId <= 0 || !alertKey.trim()) {
        throw new Error("riskAlerts update requires portfolioId and alertKey");
      }

      if (action === "ack") {
        const json = await postJson(`${apiUrl}/api/risk-alerts/ack`, {
          portfolioId,
          alertKey,
          note: String(payload.note ?? "")
        });
        const row = (json.item ?? params.data) as Row;
        return { data: { ...(params.previousData as Row), ...row, id: row.alertKey as Identifier } };
      }

      if (action === "unack") {
        const json = await postJson(`${apiUrl}/api/risk-alerts/unack`, {
          portfolioId,
          alertKey
        });
        const row = (json.item ?? params.data) as Row;
        return { data: { ...(params.previousData as Row), ...row, id: row.alertKey as Identifier } };
      }

      if (action === "workflow") {
        const json = await postJson(`${apiUrl}/api/risk-alerts/workflow`, {
          portfolioId,
          alertKey,
          workflowStatus: String(payload.workflowStatus ?? "OPEN"),
          note: String(payload.note ?? ""),
          assignee: String(payload.assignee ?? "")
        });
        const row = (json.item ?? params.data) as Row;
        return { data: { ...(params.previousData as Row), ...row, id: row.alertKey as Identifier } };
      }

      throw new Error("riskAlerts update requires meta.action in [ack, unack, workflow]");
    }

    if (resource === "orderHealth") {
      const action = (params.meta as Record<string, unknown> | undefined)?.action as string | undefined;
      if (action !== "remediateStale") {
        throw new Error("orderHealth update requires meta.action=remediateStale");
      }

      const payload = params.data as Record<string, unknown>;
      const json = await postJson(`${apiUrl}/api/order-health/remediate-stale`, {
        portfolioId: Number(payload.portfolioId),
        symbol: payload.symbol == null ? null : String(payload.symbol),
        staleMinutes: Number(payload.staleMinutes ?? 30),
        reason: String(payload.reason ?? "")
      });

      return {
        data: {
          ...(params.previousData as Row),
          id: params.id,
          canceledCount: Number(json.canceledCount ?? 0),
          staleOrderCount: Number(json.staleOrderCount ?? 0),
          evaluatedOpenOrderCount: Number(json.evaluatedOpenOrderCount ?? 0),
          remediationExecutedAt: String(json.executedAt ?? "")
        }
      };
    }

    if (resource === "users") {
      const action = (params.meta as Record<string, unknown> | undefined)?.action as string | undefined;
      if (!action) {
        throw new Error("users update requires meta.action");
      }

      if (action === "status") {
        const json = await putJson(`${apiUrl}/api/users/${params.id}/status`, {
          status: (params.data as Record<string, unknown>).status
        });
        return {
          data: {
            ...(params.previousData as Row),
            ...json,
            id: json.userId
          }
        };
      }

      if (action === "resetPassword") {
        const json = await postJson(`${apiUrl}/api/users/${params.id}/reset-password`, {});
        return {
          data: {
            ...(params.previousData as Row),
            ...json,
            id: json.userId
          }
        };
      }

      if (action === "roles") {
        const json = await putJson(`${apiUrl}/api/users/${params.id}/roles`, {
          roleCodes: (params.data as Record<string, unknown>).roleCodes
        });
        return {
          data: {
            ...(params.previousData as Row),
            ...json,
            id: json.userId
          }
        };
      }

      throw new Error(`unsupported users update action: ${action}`);
    }

    if (resource === "accountSessions") {
      const action = (params.meta as Record<string, unknown> | undefined)?.action as string | undefined;
      if (action !== "revoke") {
        throw new Error("accountSessions update requires meta.action=revoke");
      }
      const json = await postJson(`${apiUrl}/api/account/sessions/${params.id}/revoke`, {});
      return {
        data: {
          ...(params.previousData as Row),
          ...json,
          id: json.sessionId
        }
      };
    }

    throw new Error("Not implemented");
  },

  async updateMany() {
    return { data: [] };
  },

  async create(resource: string, params: CreateParams): Promise<CreateResult<Row>> {
    if (resource === "orders") {
      const json = await postJson(`${apiUrl}/api/orders`, params.data);
      return { data: { ...json, id: json.orderId } };
    }

    if (resource === "trades") {
      const json = await postJson(`${apiUrl}/api/trades/events`, params.data);
      return { data: { ...json, id: json.tradeId } };
    }

    if (resource === "riskLimits") {
      const json = await putJson(`${apiUrl}/api/risk-limits`, params.data);
      const row = (json.items?.[0] ?? params.data) as Row;
      return { data: { ...row, id: row.portfolioId as Identifier } };
    }

    if (resource === "users") {
      const json = await postJson(`${apiUrl}/api/users`, normalizeUserCreatePayload(params.data as Record<string, unknown>));
      return { data: { ...json, id: json.userId } };
    }

    if (resource === "roles") {
      const json = await postJson(`${apiUrl}/api/roles`, normalizeRoleCreatePayload(params.data as Record<string, unknown>));
      return { data: { ...json, id: json.roleId } };
    }

    if (resource === "menus") {
      const json = await postJson(`${apiUrl}/api/menus`, normalizeMenuCreatePayload(params.data as Record<string, unknown>));
      return { data: { ...json, id: json.menuId } };
    }

    if (resource === "menuPermissions") {
      const json = await putJson(
        `${apiUrl}/api/menu-permissions`,
        normalizeMenuPermissionPayload(params.data as Record<string, unknown>)
      );
      return { data: { ...json, id: json.menuPermissionId } };
    }

    if (resource === "journalVouchers") {
      const json = await postJson(`${apiUrl}/api/journal-vouchers`, params.data);
      return { data: { ...json, id: json.voucherId } };
    }

    if (resource === "orderBulkActions") {
      const payload = params.data as Record<string, unknown>;
      const action = String(payload.action ?? "").toLowerCase();
      if (!["cancel", "reject"].includes(action)) {
        throw new Error("orderBulkActions requires action in [cancel, reject]");
      }
      const orderIds = Array.isArray(payload.orderIds)
        ? payload.orderIds.map((item) => Number(item)).filter((num) => Number.isFinite(num) && num > 0)
        : [];
      if (orderIds.length === 0) {
        throw new Error("orderBulkActions requires non-empty orderIds");
      }

      const json = await postJson(`${apiUrl}/api/orders/bulk/${action}`, {
        orderIds,
        reason: String(payload.reason ?? "")
      });
      return { data: { ...json, id: `${action}-${Date.now()}` } };
    }

    if (resource === "savedViews") {
      const json = await postJson(`${apiUrl}/api/saved-views`, normalizeSavedViewPayload(params.data as Record<string, unknown>));
      return { data: { ...json, id: json.viewId } };
    }

    if (resource === "savedViewDefaults") {
      const payload = params.data as Record<string, unknown>;
      const json = await postJson(`${apiUrl}/api/saved-views/default`, {
        resourceKey: String(payload.resourceKey ?? ""),
        viewId: Number(payload.viewId)
      });
      return {
        data: {
          ...json,
          id: `${json.resourceKey}-${json.viewId}`
        }
      };
    }

    if (resource === "accountWorkQueueActions") {
      const payload = params.data as Record<string, unknown>;
      const action = String(payload.action ?? "");

      if (action === "remediateStaleOrders") {
        const json = await postJson(`${apiUrl}/api/account/work-queue/actions/remediate-stale-orders`, {
          portfolioId: Number(payload.portfolioId),
          staleMinutes: Number(payload.staleMinutes ?? 30),
          reason: String(payload.reason ?? "dashboard quick action")
        });
        return { data: { ...json, id: `remediate-${Date.now()}` } };
      }

      if (action === "revokeOtherSessions") {
        const json = await postJson(`${apiUrl}/api/account/work-queue/actions/revoke-other-sessions`, {
          portfolioId: normalizeOptionalNumber(payload.portfolioId),
          reason: String(payload.reason ?? "dashboard quick action")
        });
        return { data: { ...json, id: `revoke-${Date.now()}` } };
      }

      if (action === "postApprovedVouchers") {
        const json = await postJson(`${apiUrl}/api/account/work-queue/actions/post-approved-vouchers`, {
          portfolioId: Number(payload.portfolioId),
          limit: Number(payload.limit ?? 20),
          reason: String(payload.reason ?? "dashboard voucher posting")
        });
        return { data: { ...json, id: `post-approved-vouchers-${Date.now()}` } };
      }

      if (action === "approveDraftVouchers") {
        const json = await postJson(`${apiUrl}/api/account/work-queue/actions/approve-draft-vouchers`, {
          portfolioId: Number(payload.portfolioId),
          limit: Number(payload.limit ?? 20),
          reason: String(payload.reason ?? "dashboard voucher approval")
        });
        return { data: { ...json, id: `approve-draft-vouchers-${Date.now()}` } };
      }

      if (action === "pauseTrading") {
        const json = await postJson(`${apiUrl}/api/account/work-queue/actions/pause-trading`, {
          portfolioId: Number(payload.portfolioId),
          reason: String(payload.reason ?? "dashboard critical risk response")
        });
        return { data: { ...json, id: `pause-trading-${Date.now()}` } };
      }

      if (action === "emergencyRiskResponse") {
        const json = await postJson(`${apiUrl}/api/account/work-queue/actions/emergency-risk-response`, {
          portfolioId: Number(payload.portfolioId),
          reason: String(payload.reason ?? "dashboard emergency risk response"),
          cancelOpenOrders: payload.cancelOpenOrders === false ? false : true
        });
        return { data: { ...json, id: `emergency-risk-response-${Date.now()}` } };
      }

      if (action === "resumeTrading") {
        const json = await postJson(`${apiUrl}/api/account/work-queue/actions/resume-trading`, {
          portfolioId: Number(payload.portfolioId),
          reason: String(payload.reason ?? "dashboard trading resume"),
          force: payload.force === true
        });
        return { data: { ...json, id: `resume-trading-${Date.now()}` } };
      }

      throw new Error(`unsupported accountWorkQueueActions action: ${action}`);
    }

    return { data: { ...(params.data as Row), id: 1 } };
  },

  async delete(resource: string, params: DeleteParams<Row>): Promise<DeleteResult<Row>> {
    if (resource === "orders") {
      const json = await deleteJson(`${apiUrl}/api/orders/${params.id}`);
      return { data: { ...(params.previousData as Row), ...json, id: json.orderId } };
    }

    if (resource === "users") {
      const json = await deleteJson(`${apiUrl}/api/users/${params.id}`);
      return { data: { ...(params.previousData as Row), ...json, id: json.userId } };
    }

    if (resource === "roles") {
      const json = await deleteJson(`${apiUrl}/api/roles/${params.id}`);
      return { data: { ...(params.previousData as Row), ...json, id: json.roleId } };
    }

    if (resource === "menus") {
      const json = await deleteJson(`${apiUrl}/api/menus/${params.id}`);
      return { data: { ...(params.previousData as Row), ...json, id: json.menuId } };
    }

    if (resource === "menuPermissions") {
      const json = await deleteJson(`${apiUrl}/api/menu-permissions/${params.id}`);
      return { data: { ...(params.previousData as Row), ...json, id: json.menuPermissionId } };
    }

    if (resource === "savedViews") {
      const json = await deleteJson(`${apiUrl}/api/saved-views/${params.id}`);
      return { data: { ...(params.previousData as Row), ...json, id: json.viewId } };
    }

    throw new Error(`delete not implemented for resource: ${resource}`);
  },

  async deleteMany() {
    return { data: [] };
  }
};
