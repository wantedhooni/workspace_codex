import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  MenuItem,
  Select,
  Stack,
  Switch,
  TextField,
  Typography
} from "@mui/material";
import { useEffect, useMemo, useState } from "react";
import {
  useDataProvider,
  useGetList,
  useListContext,
  useNotify,
  usePermissions,
  useRefresh
} from "react-admin";
import { useSelectedPortfolioId } from "../portfolio/portfolioSelection";
import type { AppPermissions } from "../providers/authProvider";

type SavedViewRow = {
  id: number;
  viewId: number;
  viewName: string;
  description?: string;
  shared: boolean;
  ownerEmail: string;
  filters?: Record<string, string>;
};

type SavedViewDefaultRow = {
  id: string;
  resourceKey: string;
  viewId: number;
  viewName: string;
  shared: boolean;
  ownerEmail: string;
  pinnedAt?: string;
};

type WorkbenchSummary = {
  portfolioId?: number;
  staleMinutes?: number;
  totalOrderCount?: number;
  openOrderCount?: number;
  staleOrderCount?: number;
  openNotional?: number;
  dailyTurnover?: number;
  turnoverUsagePct?: number;
  totalPnl?: number;
};

type WorkbenchStatusCounter = {
  status: string;
  count: number;
};

type WorkbenchTopSymbol = {
  symbol: string;
  openOrderCount: number;
  openNotional: number;
  fillRatePct: number;
};

type WorkbenchRow = {
  summary?: WorkbenchSummary;
  statusCounters?: WorkbenchStatusCounter[];
  topSymbols?: WorkbenchTopSymbol[];
};

const money = new Intl.NumberFormat("ko-KR", {
  minimumFractionDigits: 0,
  maximumFractionDigits: 0
});

const pct = (value: number) => `${Number(value ?? 0).toFixed(2)}%`;

function cleanFilters(filters: Record<string, unknown>) {
  const next: Record<string, string> = {};
  Object.entries(filters).forEach(([key, value]) => {
    if (value === null || value === undefined) return;
    const text = String(value).trim();
    if (!text) return;
    next[key] = text;
  });
  return next;
}

function normalizeOrderFilters(
  filters: Record<string, unknown>,
  selectedPortfolioId: number
) {
  const normalized: Record<string, unknown> = {};
  const portfolioId = Number(filters.portfolioId ?? selectedPortfolioId);
  normalized.portfolioId = Number.isFinite(portfolioId) && portfolioId > 0 ? Math.trunc(portfolioId) : selectedPortfolioId;

  const symbol = String(filters.symbol ?? "").trim().toUpperCase();
  if (symbol) normalized.symbol = symbol;

  const status = String(filters.status ?? "").trim().toUpperCase();
  if (status) normalized.status = status;

  const side = String(filters.side ?? "").trim().toUpperCase();
  if (side) normalized.side = side;

  const orderType = String(filters.orderType ?? "").trim().toUpperCase();
  if (orderType) normalized.orderType = orderType;

  const timeInForce = String(filters.timeInForce ?? "").trim().toUpperCase();
  if (timeInForce) normalized.timeInForce = timeInForce;

  const minQuantity = Number(filters.minQuantity);
  if (Number.isFinite(minQuantity) && minQuantity >= 0) normalized.minQuantity = minQuantity;

  const maxQuantity = Number(filters.maxQuantity);
  if (Number.isFinite(maxQuantity) && maxQuantity >= 0) normalized.maxQuantity = maxQuantity;

  return normalized;
}

type MetricProps = {
  title: string;
  value: string;
};

function MetricCard({ title, value }: MetricProps) {
  return (
    <Card variant="outlined" sx={{ minWidth: 160 }}>
      <CardContent sx={{ py: 1.2, "&:last-child": { pb: 1.2 } }}>
        <Typography variant="caption" color="text.secondary">
          {title}
        </Typography>
        <Typography variant="h6" fontWeight={700}>
          {value}
        </Typography>
      </CardContent>
    </Card>
  );
}

export function OrderWorkbenchPanel() {
  const { filterValues, setFilters } = useListContext();
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const notify = useNotify();
  const refresh = useRefresh();
  const dataProvider = useDataProvider();
  const { permissions } = usePermissions<AppPermissions>();
  const menuPermission = (permissions as AppPermissions | undefined)?.menuPermissions?.savedViews;
  const canReadSavedViews = Boolean(menuPermission?.canRead);
  const canCreateSavedView = Boolean(menuPermission?.canCreate);
  const canDeleteSavedView = Boolean(menuPermission?.canDelete);

  const appliedFilters = useMemo(
    () => normalizeOrderFilters((filterValues ?? {}) as Record<string, unknown>, selectedPortfolioId),
    [filterValues, selectedPortfolioId]
  );

  const workbenchQuery = useGetList<WorkbenchRow>("orderWorkbench", {
    pagination: { page: 1, perPage: 1 },
    sort: { field: "generatedAt", order: "DESC" },
    filter: {
      portfolioId: Number(appliedFilters.portfolioId ?? selectedPortfolioId),
      staleMinutes: 30,
      topN: 6
    }
  });

  const savedViewsQuery = useGetList<SavedViewRow>("savedViews", {
    pagination: { page: 1, perPage: 100 },
    sort: { field: "viewName", order: "ASC" },
    filter: { resourceKey: "orders" }
  });

  const savedViewDefaultQuery = useGetList<SavedViewDefaultRow>("savedViewDefaults", {
    pagination: { page: 1, perPage: 1 },
    sort: { field: "pinnedAt", order: "DESC" },
    filter: { resourceKey: "orders" }
  });

  const savedViews = useMemo(
    () =>
      canReadSavedViews
        ? ((savedViewsQuery.data ?? []) as SavedViewRow[]).sort((a, b) => a.viewName.localeCompare(b.viewName))
        : [],
    [canReadSavedViews, savedViewsQuery.data]
  );

  const [selectedViewId, setSelectedViewId] = useState<string>("");
  const [saveDialogOpen, setSaveDialogOpen] = useState(false);
  const [newViewName, setNewViewName] = useState("");
  const [newViewDescription, setNewViewDescription] = useState("");
  const [newViewShared, setNewViewShared] = useState(true);
  const [defaultAppliedOnce, setDefaultAppliedOnce] = useState(false);

  const row = (workbenchQuery.data?.[0] ?? {}) as WorkbenchRow;
  const summary = (row.summary ?? {}) as WorkbenchSummary;
  const statusCounters = (row.statusCounters ?? []) as WorkbenchStatusCounter[];
  const topSymbols = (row.topSymbols ?? []) as WorkbenchTopSymbol[];
  const defaultView = (savedViewDefaultQuery.data?.[0] ?? null) as SavedViewDefaultRow | null;

  const statusLevel =
    Number(summary.turnoverUsagePct ?? 0) >= 95
      ? ("error" as const)
      : Number(summary.turnoverUsagePct ?? 0) >= 80
      ? ("warning" as const)
      : ("success" as const);

  const applyStatusFilter = (status?: string) => {
    const next = normalizeOrderFilters(
      {
        ...appliedFilters,
        status: status ?? ""
      },
      selectedPortfolioId
    );
    setFilters(next, undefined, false);
  };

  const applySavedView = () => {
    const target = savedViews.find((item) => String(item.viewId ?? item.id) === selectedViewId);
    if (!target) {
      notify("적용할 저장 뷰를 선택해 주세요.", { type: "warning" });
      return;
    }

    const next = normalizeOrderFilters(target.filters ?? {}, selectedPortfolioId);
    setFilters(next, undefined, false);
    notify(`저장 뷰 적용: ${target.viewName}`, { type: "info" });
  };

  const applyDefaultView = () => {
    if (!defaultView || !defaultView.viewId) {
      notify("기본 뷰가 설정되어 있지 않습니다.", { type: "warning" });
      return;
    }
    const target = savedViews.find((item) => Number(item.viewId ?? item.id) === Number(defaultView.viewId));
    if (!target) {
      notify("기본 뷰를 찾을 수 없습니다.", { type: "warning" });
      return;
    }
    const next = normalizeOrderFilters(target.filters ?? {}, selectedPortfolioId);
    setFilters(next, undefined, false);
    setSelectedViewId(String(target.viewId ?? target.id));
    notify(`기본 뷰 적용: ${target.viewName}`, { type: "info" });
  };

  const saveCurrentView = async () => {
    const trimmedName = newViewName.trim();
    if (!trimmedName) {
      notify("저장 뷰 이름을 입력해 주세요.", { type: "warning" });
      return;
    }

    try {
      await dataProvider.create("savedViews", {
        data: {
          resourceKey: "orders",
          viewName: trimmedName,
          description: newViewDescription.trim(),
          shared: newViewShared,
          filters: cleanFilters(appliedFilters)
        }
      });
      notify("저장 뷰가 생성되었습니다.", { type: "info" });
      setSaveDialogOpen(false);
      setNewViewName("");
      setNewViewDescription("");
      setNewViewShared(true);
      refresh();
    } catch (error) {
      notify(error instanceof Error ? error.message : "저장 뷰 생성 실패", { type: "warning" });
    }
  };

  const deleteSelectedView = async () => {
    const target = savedViews.find((item) => String(item.viewId ?? item.id) === selectedViewId);
    if (!target) {
      notify("삭제할 저장 뷰를 선택해 주세요.", { type: "warning" });
      return;
    }

    try {
      await dataProvider.delete("savedViews", {
        id: target.viewId ?? target.id,
        previousData: target
      });
      setSelectedViewId("");
      notify("저장 뷰가 삭제되었습니다.", { type: "info" });
      refresh();
    } catch (error) {
      notify(error instanceof Error ? error.message : "저장 뷰 삭제 실패", { type: "warning" });
    }
  };

  const pinSelectedView = async () => {
    const target = savedViews.find((item) => String(item.viewId ?? item.id) === selectedViewId);
    if (!target) {
      notify("기본 뷰로 고정할 저장 뷰를 선택해 주세요.", { type: "warning" });
      return;
    }

    try {
      await dataProvider.create("savedViewDefaults", {
        data: {
          resourceKey: "orders",
          viewId: target.viewId ?? target.id
        }
      });
      notify(`기본 뷰 설정 완료: ${target.viewName}`, { type: "info" });
      refresh();
    } catch (error) {
      notify(error instanceof Error ? error.message : "기본 뷰 설정 실패", { type: "warning" });
    }
  };

  useEffect(() => {
    if (defaultAppliedOnce) {
      return;
    }
    if (!canReadSavedViews) {
      setDefaultAppliedOnce(true);
      return;
    }
    if (!defaultView || !defaultView.viewId) {
      return;
    }

    const rawFilters = (filterValues ?? {}) as Record<string, unknown>;
    const hasInteractiveFilter =
      String(rawFilters.symbol ?? "").trim().length > 0 ||
      String(rawFilters.status ?? "").trim().length > 0 ||
      String(rawFilters.side ?? "").trim().length > 0 ||
      String(rawFilters.orderType ?? "").trim().length > 0 ||
      String(rawFilters.timeInForce ?? "").trim().length > 0 ||
      String(rawFilters.minQuantity ?? "").trim().length > 0 ||
      String(rawFilters.maxQuantity ?? "").trim().length > 0;
    if (hasInteractiveFilter) {
      setDefaultAppliedOnce(true);
      return;
    }

    const target = savedViews.find((item) => Number(item.viewId ?? item.id) === Number(defaultView.viewId));
    if (!target) {
      return;
    }

    const next = normalizeOrderFilters(target.filters ?? {}, selectedPortfolioId);
    setFilters(next, undefined, false);
    setSelectedViewId(String(target.viewId ?? target.id));
    setDefaultAppliedOnce(true);
  }, [
    canReadSavedViews,
    defaultAppliedOnce,
    defaultView,
    filterValues,
    savedViews,
    selectedPortfolioId,
    setFilters
  ]);

  return (
    <Stack spacing={1.25} sx={{ mb: 1 }}>
      <Stack direction="row" spacing={1} alignItems="center" useFlexGap flexWrap="wrap">
        <Typography variant="subtitle1" fontWeight={700}>
          Trading Workbench
        </Typography>
        <Chip label={`오픈 주문 ${Number(summary.openOrderCount ?? 0)}건`} size="small" variant="outlined" />
        <Chip
          label={`회전 사용률 ${pct(Number(summary.turnoverUsagePct ?? 0))}`}
          size="small"
          color={statusLevel}
          variant={statusLevel === "success" ? "outlined" : "filled"}
        />
        {workbenchQuery.isPending ? <Chip label="집계 로딩 중..." size="small" /> : null}
      </Stack>

      <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
        <MetricCard title="총 주문" value={String(Number(summary.totalOrderCount ?? 0))} />
        <MetricCard title="스테일 주문" value={String(Number(summary.staleOrderCount ?? 0))} />
        <MetricCard title="오픈 주문금액" value={money.format(Number(summary.openNotional ?? 0))} />
        <MetricCard title="일일 회전금액" value={money.format(Number(summary.dailyTurnover ?? 0))} />
        <MetricCard title="총 손익" value={money.format(Number(summary.totalPnl ?? 0))} />
      </Stack>

      <Stack direction="row" spacing={1} alignItems="center" useFlexGap flexWrap="wrap">
        <Typography variant="body2" color="text.secondary">
          상태 퀵필터
        </Typography>
        <Button size="small" variant="text" onClick={() => applyStatusFilter(undefined)}>
          전체
        </Button>
        {statusCounters.map((statusRow) => (
          <Chip
            key={statusRow.status}
            label={`${statusRow.status} (${statusRow.count})`}
            size="small"
            onClick={() => applyStatusFilter(statusRow.status)}
            color={String(appliedFilters.status ?? "").toUpperCase() === statusRow.status ? "primary" : "default"}
            variant={String(appliedFilters.status ?? "").toUpperCase() === statusRow.status ? "filled" : "outlined"}
          />
        ))}
      </Stack>

      {canReadSavedViews ? (
        <Stack direction="row" spacing={1} alignItems="center" useFlexGap flexWrap="wrap">
          <Typography variant="body2" color="text.secondary">
            저장 뷰
          </Typography>
          <FormControl size="small" sx={{ minWidth: 260 }}>
            <Select
              displayEmpty
              value={selectedViewId}
              onChange={(event) => setSelectedViewId(String(event.target.value))}
            >
              <MenuItem value="">
                <em>저장 뷰 선택</em>
              </MenuItem>
              {savedViews.map((view) => (
                <MenuItem key={view.viewId ?? view.id} value={String(view.viewId ?? view.id)}>
                  {view.viewName} {view.shared ? "(공용)" : "(개인)"}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <Button size="small" variant="outlined" onClick={applySavedView} disabled={!selectedViewId}>
            적용
          </Button>
          <Button size="small" variant="text" onClick={applyDefaultView} disabled={!defaultView?.viewId}>
            기본뷰 적용
          </Button>
          <Button size="small" variant="text" onClick={pinSelectedView} disabled={!selectedViewId}>
            기본뷰 고정
          </Button>
          {canCreateSavedView ? (
            <Button size="small" variant="contained" onClick={() => setSaveDialogOpen(true)}>
              현재 필터 저장
            </Button>
          ) : null}
          {canDeleteSavedView ? (
            <Button size="small" color="error" variant="text" onClick={deleteSelectedView} disabled={!selectedViewId}>
              선택 뷰 삭제
            </Button>
          ) : null}
        </Stack>
      ) : null}

      {defaultView?.viewId ? (
        <Stack direction="row" spacing={1} alignItems="center" useFlexGap flexWrap="wrap">
          <Chip
            size="small"
            color="primary"
            variant="outlined"
            label={`기본 뷰: ${defaultView.viewName} (${defaultView.shared ? "공용" : "개인"})`}
          />
        </Stack>
      ) : null}

      {topSymbols.length > 0 ? (
        <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
          {topSymbols.map((symbol) => (
            <Chip
              key={symbol.symbol}
              label={`${symbol.symbol} · open ${symbol.openOrderCount} · fill ${pct(Number(symbol.fillRatePct ?? 0))}`}
              variant="outlined"
            />
          ))}
        </Stack>
      ) : (
        <Alert severity="info">상위 심볼 집계 데이터가 없습니다.</Alert>
      )}

      <Dialog open={saveDialogOpen} onClose={() => setSaveDialogOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>현재 필터 저장</DialogTitle>
        <DialogContent>
          <Stack spacing={1.25} sx={{ pt: 0.5 }}>
            <TextField
              label="뷰 이름"
              value={newViewName}
              onChange={(event) => setNewViewName(event.target.value)}
              autoFocus
              fullWidth
            />
            <TextField
              label="설명"
              value={newViewDescription}
              onChange={(event) => setNewViewDescription(event.target.value)}
              fullWidth
            />
            <Stack direction="row" spacing={1} alignItems="center">
              <Switch checked={newViewShared} onChange={(_, checked) => setNewViewShared(checked)} />
              <Typography variant="body2">공용 뷰로 저장</Typography>
            </Stack>
            <Box sx={{ px: 1, py: 0.75, borderRadius: 1, backgroundColor: "action.hover" }}>
              <Typography variant="caption" color="text.secondary">
                저장될 필터
              </Typography>
              <Typography component="pre" variant="body2" sx={{ m: 0, whiteSpace: "pre-wrap", wordBreak: "break-all" }}>
                {JSON.stringify(cleanFilters(appliedFilters), null, 2)}
              </Typography>
            </Box>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSaveDialogOpen(false)}>닫기</Button>
          <Button variant="contained" onClick={saveCurrentView}>
            저장
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}
