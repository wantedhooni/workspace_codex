import {
  Alert,
  Button,
  Card,
  CardActions,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  List as MuiList,
  ListItem,
  ListItemText,
  Stack,
  Typography
} from "@mui/material";
import { useMemo, useState } from "react";
import { useDataProvider, useGetList, useNotify, usePermissions } from "react-admin";
import type { AppPermissions } from "../providers/authProvider";
import { usePortfolioCatalog } from "../portfolio/portfolioCatalog";
import { useSelectedPortfolioId } from "../portfolio/portfolioSelection";
import type { ButtonProps } from "@mui/material/Button";

type SummaryRow = {
  grossExposure?: number;
  marketValue?: number;
  realizedPnl?: number;
  unrealizedPnl?: number;
  totalPnl?: number;
  dailyTurnover?: number;
  turnoverUsagePct?: number;
  openOrderCount?: number;
  filledOrderCount?: number;
  tradeCount?: number;
  positionCount?: number;
};

type RiskLimitRow = {
  maxDailyTurnover?: number;
  maxOrderNotional?: number;
};

type WorkQueueSummary = {
  portfolioId?: number;
  openOrderCount?: number;
  staleOrderCount?: number;
  criticalRiskCount?: number;
  warningRiskCount?: number;
  approvedVoucherCount?: number;
  draftVoucherCount?: number;
  activeSessionCount?: number;
};

type WorkQueueAlert = {
  severity?: string;
  message?: string;
};

type WorkQueueTask = {
  taskKey?: string;
  title?: string;
  description?: string;
  severity?: string;
  count?: number;
  actionLabel?: string;
  path?: string;
  actionEnabled?: boolean;
};

type WorkQueueRow = {
  summary?: WorkQueueSummary;
  alerts?: WorkQueueAlert[];
  tasks?: WorkQueueTask[];
  generatedAt?: string;
};

type ActivityFeedRow = {
  activityKey?: string;
  category?: string;
  severity?: string;
  title?: string;
  description?: string;
  path?: string;
  actor?: string | null;
  occurredAt?: string;
};

type ResumeTradingResponse = {
  resumed?: boolean;
  blockedCriticalCount?: number;
  blockedCodes?: unknown;
  blockedMessages?: unknown;
};

type ProfitPlaybookAction = {
  actionKey?: string;
  severity?: string;
  blocker?: boolean;
  title?: string;
  description?: string;
  expectedImpact?: string;
  ownerRole?: string;
  horizon?: string;
  path?: string;
};

type ProfitPlaybookRow = {
  portfolioId?: number;
  objective?: string;
  objectiveDetail?: string;
  strategyFocus?: string;
  marketRegime?: string;
  executionGuideline?: string;
  tradable?: boolean;
  priorityScore?: number;
  blockerCount?: number;
  generatedAt?: string;
  actions?: ProfitPlaybookAction[];
};

type ProfitPlaybookFeedbackSnapshot = {
  healthScore?: number;
  healthStatus?: string;
  totalPnl?: number;
  turnoverUsagePct?: number;
  openOrderCount?: number;
  criticalAlertCount?: number;
  warnAlertCount?: number;
  tradingEnabled?: boolean;
  capturedAt?: string;
};

type ProfitPlaybookFeedbackRow = {
  feedbackId?: number;
  portfolioId?: number;
  actionKey?: string;
  actionTitle?: string;
  sourceTaskKey?: string;
  outcomeStatus?: string;
  reason?: string;
  executedBy?: string;
  executedAt?: string;
  beforeSnapshot?: ProfitPlaybookFeedbackSnapshot;
  afterSnapshot?: ProfitPlaybookFeedbackSnapshot;
  deltaTotalPnl?: number;
  deltaTurnoverUsagePct?: number;
  deltaOpenOrderCount?: number;
  deltaCriticalAlertCount?: number;
  deltaWarnAlertCount?: number;
  outcomeEvaluation?: string;
};

const money = new Intl.NumberFormat("ko-KR", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
});

function signedNumber(value: unknown, fractionDigits = 2) {
  const num = Number(value ?? 0);
  if (!Number.isFinite(num) || num === 0) {
    return num.toFixed(fractionDigits);
  }
  return `${num > 0 ? "+" : ""}${num.toFixed(fractionDigits)}`;
}

function signedInteger(value: unknown) {
  const num = Number(value ?? 0);
  if (!Number.isFinite(num) || num === 0) {
    return "0";
  }
  return `${num > 0 ? "+" : ""}${Math.trunc(num)}`;
}

function MetricCard({ title, value }: { title: string; value: string }) {
  return (
    <Card>
      <CardContent>
        <Stack spacing={0.5}>
          <Typography color="text.secondary" variant="body2">
            {title}
          </Typography>
          <Typography variant="h5">{value}</Typography>
        </Stack>
      </CardContent>
    </Card>
  );
}

function severityColor(severity: string | undefined): "default" | "success" | "warning" | "error" | "info" {
  if (severity === "CRITICAL") return "error";
  if (severity === "WARN") return "warning";
  if (severity === "INFO") return "info";
  return "default";
}

function jumpToHash(path: string | undefined) {
  if (!path) return;
  if (path.startsWith("/#/")) {
    window.location.hash = path.slice(2);
    return;
  }
  if (path.startsWith("#/")) {
    window.location.hash = path.slice(1);
    return;
  }
  if (path.startsWith("/")) {
    window.location.hash = path;
    return;
  }
  window.location.hash = `/${path}`;
}

export default function Dashboard() {
  const [portfolioId] = useSelectedPortfolioId(1);
  const { formatPortfolioLabel } = usePortfolioCatalog();
  const { permissions } = usePermissions<AppPermissions>();
  const dataProvider = useDataProvider();
  const notify = useNotify();
  const [actionLoadingKey, setActionLoadingKey] = useState<string | null>(null);
  const [resumeGuardDialog, setResumeGuardDialog] = useState<{
    open: boolean;
    blockedCount: number;
    blockedCodes: string[];
    blockedMessages: string[];
  }>({
    open: false,
    blockedCount: 0,
    blockedCodes: [],
    blockedMessages: []
  });

  const summaryQuery = useGetList("portfolioSummaries", {
    pagination: { page: 1, perPage: 1 },
    sort: { field: "portfolioId", order: "ASC" },
    filter: { portfolioId }
  });

  const riskLimitQuery = useGetList("riskLimits", {
    pagination: { page: 1, perPage: 1 },
    sort: { field: "portfolioId", order: "ASC" },
    filter: { portfolioId }
  });

  const workQueueQuery = useGetList("accountWorkQueue", {
    pagination: { page: 1, perPage: 1 },
    sort: { field: "generatedAt", order: "DESC" },
    filter: { portfolioId, staleMinutes: 30, topN: 6 }
  });

  const activityFeedQuery = useGetList("accountActivityFeed", {
    pagination: { page: 1, perPage: 6 },
    sort: { field: "occurredAt", order: "DESC" },
    filter: { portfolioId, limit: 6 }
  });

  const playbookQuery = useGetList("portfolioProfitPlaybook", {
    pagination: { page: 1, perPage: 1 },
    sort: { field: "generatedAt", order: "DESC" },
    filter: { portfolioId }
  });

  const playbookFeedbackQuery = useGetList("portfolioProfitPlaybookFeedback", {
    pagination: { page: 1, perPage: 5 },
    sort: { field: "executedAt", order: "DESC" },
    filter: { portfolioId, limit: 5 }
  });

  const metrics = useMemo(() => {
    const summary = ((summaryQuery.data ?? [])[0] ?? {}) as SummaryRow;
    const risk = ((riskLimitQuery.data ?? [])[0] ?? {}) as RiskLimitRow;
    const openOrderCount = Number(summary.openOrderCount ?? 0);
    const filledOrderCount = Number(summary.filledOrderCount ?? 0);

    return {
      grossExposure: Number(summary.grossExposure ?? 0),
      marketValue: Number(summary.marketValue ?? 0),
      realized: Number(summary.realizedPnl ?? 0),
      unrealized: Number(summary.unrealizedPnl ?? 0),
      totalPnl: Number(summary.totalPnl ?? 0),
      turnover: Number(summary.dailyTurnover ?? 0),
      turnoverUsagePct: Number(summary.turnoverUsagePct ?? 0),
      openOrderCount,
      filledOrderCount,
      orderCount: openOrderCount + filledOrderCount,
      tradeCount: Number(summary.tradeCount ?? 0),
      positionCount: Number(summary.positionCount ?? 0),
      maxDailyTurnover: Number(risk.maxDailyTurnover ?? 0),
      maxOrderNotional: Number(risk.maxOrderNotional ?? 0)
    };
  }, [riskLimitQuery.data, summaryQuery.data]);

  const roleCodes = useMemo(
    () => new Set((permissions as AppPermissions | undefined)?.roleCodes ?? []),
    [permissions]
  );
  const isAdmin = roleCodes.has("ADMIN");
  const isTrader = isAdmin || roleCodes.has("TRADER");
  const isRisk = isAdmin || roleCodes.has("RISK");
  const isViewer = isAdmin || roleCodes.has("VIEWER");

  const statusLevel = useMemo(() => {
    if (metrics.turnoverUsagePct >= 95) return { label: "CRITICAL", color: "error" as const };
    if (metrics.turnoverUsagePct >= 80) return { label: "WARN", color: "warning" as const };
    return { label: "STABLE", color: "success" as const };
  }, [metrics.turnoverUsagePct]);

  const alertMessages = useMemo(() => {
    const rows: string[] = [];
    if (metrics.turnoverUsagePct >= 95) {
      rows.push(`일일 회전한도 사용률이 ${metrics.turnoverUsagePct.toFixed(2)}% 입니다.`);
    }
    if (metrics.openOrderCount > 40) {
      rows.push(`오픈 주문이 ${metrics.openOrderCount}건으로 높습니다.`);
    }
    if (metrics.totalPnl < 0) {
      rows.push(`총 손익이 음수(${money.format(metrics.totalPnl)})입니다.`);
    }
    return rows;
  }, [metrics.openOrderCount, metrics.totalPnl, metrics.turnoverUsagePct]);

  const workQueue = useMemo(() => ((workQueueQuery.data ?? [])[0] ?? {}) as WorkQueueRow, [workQueueQuery.data]);
  const queueSummary = (workQueue.summary ?? {}) as WorkQueueSummary;
  const queueAlerts = Array.isArray(workQueue.alerts) ? workQueue.alerts : [];
  const queueTasks = Array.isArray(workQueue.tasks) ? workQueue.tasks : [];
  const activityRows = (activityFeedQuery.data ?? []) as unknown as ActivityFeedRow[];
  const playbookFeedbackRows = (playbookFeedbackQuery.data ?? []) as unknown as ProfitPlaybookFeedbackRow[];
  const playbook = useMemo(
    () => ((playbookQuery.data ?? [])[0] ?? {}) as ProfitPlaybookRow,
    [playbookQuery.data]
  );
  const playbookActions = Array.isArray(playbook.actions) ? playbook.actions : [];

  const reloadDashboard = async () => {
    await Promise.all([
      summaryQuery.refetch(),
      riskLimitQuery.refetch(),
      workQueueQuery.refetch(),
      activityFeedQuery.refetch(),
      playbookQuery.refetch(),
      playbookFeedbackQuery.refetch()
    ]);
  };

  const canQuickAction = (task: WorkQueueTask) => {
    return task.taskKey === "staleOrders"
      || task.taskKey === "activeSessions"
      || task.taskKey === "approvedVouchers"
      || task.taskKey === "draftVouchers"
      || task.taskKey === "criticalRiskAlerts"
      || task.taskKey === "resumeTrading";
  };

  const quickActionColor = (task: WorkQueueTask): ButtonProps["color"] => {
    if (task.taskKey === "criticalRiskAlerts") {
      return "error";
    }
    return "primary";
  };

  const handleQuickAction = async (task: WorkQueueTask) => {
    const taskKey = String(task.taskKey ?? "");
    if (!taskKey) {
      return;
    }

    setActionLoadingKey(taskKey);
    try {
      if (taskKey === "staleOrders") {
        await dataProvider.create("accountWorkQueueActions", {
          data: {
            action: "remediateStaleOrders",
            portfolioId,
            staleMinutes: 30,
            reason: "dashboard quick remediation"
          }
        });
        notify("지연 주문 정리가 완료되었습니다.", { type: "info" });
      } else if (taskKey === "activeSessions") {
        const res = await dataProvider.create("accountWorkQueueActions", {
          data: {
            action: "revokeOtherSessions",
            portfolioId,
            reason: "dashboard session hardening"
          }
        });
        const revokedCount = Number((res.data as Record<string, unknown>)?.revokedCount ?? 0);
        notify(`다른 세션 ${revokedCount}개를 해지했습니다.`, { type: "info" });
      } else if (taskKey === "approvedVouchers") {
        const res = await dataProvider.create("accountWorkQueueActions", {
          data: {
            action: "postApprovedVouchers",
            portfolioId,
            limit: 20,
            reason: "dashboard approved voucher posting"
          }
        });
        const postedCount = Number((res.data as Record<string, unknown>)?.postedCount ?? 0);
        notify(`승인 전표 ${postedCount}건을 전기 처리했습니다.`, { type: "info" });
      } else if (taskKey === "draftVouchers") {
        const res = await dataProvider.create("accountWorkQueueActions", {
          data: {
            action: "approveDraftVouchers",
            portfolioId,
            limit: 20,
            reason: "dashboard draft voucher approval"
          }
        });
        const approvedCount = Number((res.data as Record<string, unknown>)?.approvedCount ?? 0);
        notify(`초안 전표 ${approvedCount}건을 승인 처리했습니다.`, { type: "info" });
      } else if (taskKey === "criticalRiskAlerts") {
        const res = await dataProvider.create("accountWorkQueueActions", {
          data: {
            action: "emergencyRiskResponse",
            portfolioId,
            reason: "dashboard critical risk emergency response",
            cancelOpenOrders: true
          }
        });
        const canceledCount = Number((res.data as Record<string, unknown>)?.canceledCount ?? 0);
        notify(`긴급대응 완료: 거래중지 + 오픈주문 ${canceledCount}건 취소`, { type: "warning" });
      } else if (taskKey === "resumeTrading") {
        const res = await dataProvider.create("accountWorkQueueActions", {
          data: {
            action: "resumeTrading",
            portfolioId,
            reason: "dashboard resume after risk review",
            force: false
          }
        });
        const payload = (res.data ?? {}) as ResumeTradingResponse;
        const resumed = Boolean(payload.resumed);
        const blockedCount = Number(payload.blockedCriticalCount ?? 0);
        const blockedCodes = Array.isArray(payload.blockedCodes)
          ? payload.blockedCodes.map((code) => String(code))
          : [];
        const blockedMessages = Array.isArray(payload.blockedMessages)
          ? payload.blockedMessages.map((message) => String(message))
          : [];
        if (resumed) {
          notify("거래를 재개했습니다.", { type: "info" });
        } else {
          notify(`거래 재개 차단: 치명 경보 ${blockedCount}건 해소 후 재시도하세요.`, { type: "warning" });
          setResumeGuardDialog({
            open: true,
            blockedCount,
            blockedCodes,
            blockedMessages
          });
        }
      }
      await reloadDashboard();
    } catch (error) {
      notify(error instanceof Error ? error.message : "작업 실행에 실패했습니다.", { type: "warning" });
    } finally {
      setActionLoadingKey(null);
    }
  };

  const closeResumeGuardDialog = () => {
    setResumeGuardDialog((prev) => ({ ...prev, open: false }));
  };

  const handleForceResume = async () => {
    setActionLoadingKey("resumeTradingForce");
    try {
      const res = await dataProvider.create("accountWorkQueueActions", {
        data: {
          action: "resumeTrading",
          portfolioId,
          reason: "dashboard forced resume after operator confirmation",
          force: true
        }
      });
      const payload = (res.data ?? {}) as ResumeTradingResponse;
      const resumed = Boolean(payload.resumed);
      if (resumed) {
        notify("강제 거래 재개가 완료되었습니다.", { type: "warning" });
        closeResumeGuardDialog();
      } else {
        notify("강제 재개에도 실패했습니다. 리스크 상태를 재점검하세요.", { type: "warning" });
      }
      await reloadDashboard();
    } catch (error) {
      notify(error instanceof Error ? error.message : "강제 거래 재개 실행에 실패했습니다.", { type: "warning" });
    } finally {
      setActionLoadingKey(null);
    }
  };

  return (
    <Stack spacing={2}>
      <Stack direction="row" spacing={1.5} alignItems="center" useFlexGap flexWrap="wrap">
        <Typography variant="h6">퀀트 운용 대시보드 ({formatPortfolioLabel(portfolioId)})</Typography>
        <Chip label={`리스크 상태: ${statusLevel.label}`} color={statusLevel.color} size="small" />
        <Chip
          label={`권한: ${Array.from(roleCodes).sort().join(", ") || "UNKNOWN"}`}
          variant="outlined"
          size="small"
        />
      </Stack>

      <Card variant="outlined">
        <CardContent>
          <Stack spacing={1.2}>
            <Stack direction="row" spacing={1} alignItems="center" useFlexGap flexWrap="wrap">
              <Typography variant="subtitle1" fontWeight={700}>
                이 플랫폼의 목적
              </Typography>
              <Chip size="small" color="primary" label={playbook.objective || "리스크 조정 수익률 극대화"} />
              <Chip
                size="small"
                variant="outlined"
                label={`시장 국면: ${playbook.marketRegime || "ANALYZING"}`}
              />
              <Chip
                size="small"
                variant="outlined"
                color={Number(playbook.blockerCount ?? 0) > 0 ? "error" : "default"}
                label={`차단 이슈: ${Number(playbook.blockerCount ?? 0)}건`}
              />
              <Chip
                size="small"
                variant="outlined"
                label={`실행 우선순위: ${Number(playbook.priorityScore ?? 0)}`}
              />
            </Stack>

            <Typography variant="body2" color="text.secondary">
              {playbook.objectiveDetail
                || "손실 확장 구간을 먼저 차단하고, 체결 품질이 검증된 주문만 우선 집행해 누적 수익을 만든다."}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {playbook.executionGuideline || "경보/한도/체결품질 게이트 통과 후에만 익스포저를 확대합니다."}
            </Typography>

            <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
              <Chip
                size="small"
                variant="outlined"
                label={`전략 포커스: ${playbook.strategyFocus || "-"}`}
              />
              <Chip
                size="small"
                color={Boolean(playbook.tradable) ? "success" : "warning"}
                label={Boolean(playbook.tradable) ? "거래 가능" : "거래 주의/중지"}
              />
            </Stack>

            <Stack spacing={1}>
              <Typography variant="subtitle2" fontWeight={700}>
                수익 실행 루틴
              </Typography>
              {playbookActions.length === 0 ? (
                <Alert severity="info">실행 루틴을 계산 중입니다. 데이터를 새로고침해 주세요.</Alert>
              ) : (
                playbookActions.slice(0, 6).map((action, idx) => (
                  <Card key={`${action.actionKey ?? "action"}-${idx}`} variant="outlined">
                    <CardContent sx={{ pb: 1.2 }}>
                      <Stack spacing={0.6}>
                        <Stack direction="row" spacing={1} alignItems="center" useFlexGap flexWrap="wrap">
                          <Chip
                            size="small"
                            color={severityColor(String(action.severity ?? "INFO").toUpperCase())}
                            label={String(action.severity ?? "INFO").toUpperCase()}
                          />
                          <Chip
                            size="small"
                            variant="outlined"
                            color={Boolean(action.blocker) ? "error" : "default"}
                            label={Boolean(action.blocker) ? "BLOCKER" : "ACTION"}
                          />
                          <Typography variant="subtitle2" fontWeight={700}>
                            {action.title ?? "-"}
                          </Typography>
                        </Stack>
                        <Typography variant="body2" color="text.secondary">
                          {action.description ?? "-"}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          기대효과: {action.expectedImpact || "-"} · 담당: {action.ownerRole || "-"} · 기한: {action.horizon || "-"}
                        </Typography>
                      </Stack>
                    </CardContent>
                    <CardActions sx={{ px: 2, pb: 1.2 }}>
                      <Button
                        size="small"
                        variant="outlined"
                        disabled={!action.path}
                        onClick={() => jumpToHash(action.path)}
                      >
                        실행 화면 이동
                      </Button>
                    </CardActions>
                  </Card>
                ))
              )}
            </Stack>

            <Stack spacing={1}>
              <Typography variant="subtitle2" fontWeight={700}>
                실행 피드백 루프
              </Typography>
              {playbookFeedbackRows.length === 0 ? (
                <Alert severity="info">최근 액션 피드백이 없습니다. 워크큐 액션 실행 후 개선효과를 확인하세요.</Alert>
              ) : (
                playbookFeedbackRows.map((row, idx) => {
                  const after = row.afterSnapshot ?? {};
                  const evaluation = String(row.outcomeEvaluation ?? "NEUTRAL");
                  const outcome = String(row.outcomeStatus ?? "SUCCESS");
                  const title = row.actionTitle || row.actionKey || "-";
                  return (
                    <Card key={`${row.feedbackId ?? "feedback"}-${idx}`} variant="outlined">
                      <CardContent sx={{ pb: 1.2 }}>
                        <Stack spacing={0.6}>
                          <Stack direction="row" spacing={1} alignItems="center" useFlexGap flexWrap="wrap">
                            <Typography variant="subtitle2" fontWeight={700}>
                              {title}
                            </Typography>
                            <Chip
                              size="small"
                              color={outcome === "FAILED" ? "error" : "success"}
                              label={outcome}
                            />
                            <Chip
                              size="small"
                              variant="outlined"
                              color={
                                evaluation === "POSITIVE"
                                  ? "success"
                                  : evaluation === "NEGATIVE"
                                    ? "warning"
                                    : "default"
                              }
                              label={`평가: ${evaluation}`}
                            />
                            <Typography variant="caption" color="text.secondary">
                              {row.executedBy || "-"} · {row.executedAt ? new Date(row.executedAt).toLocaleString() : "-"}
                            </Typography>
                          </Stack>
                          <Typography variant="caption" color="text.secondary">
                            손익Δ {signedNumber(row.deltaTotalPnl)} · 회전율Δ {signedNumber(row.deltaTurnoverUsagePct)}%
                            {" · "}오픈주문Δ {signedInteger(row.deltaOpenOrderCount)}
                            {" · "}CRITICALΔ {signedInteger(row.deltaCriticalAlertCount)}
                            {" · "}WARNΔ {signedInteger(row.deltaWarnAlertCount)}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            현재: health {after.healthScore ?? "-"} ({after.healthStatus ?? "-"}) · trading{" "}
                            {after.tradingEnabled ? "ENABLED" : "DISABLED"}
                          </Typography>
                          {row.reason ? (
                            <Typography variant="caption" color="text.secondary">
                              사유: {row.reason}
                            </Typography>
                          ) : null}
                        </Stack>
                      </CardContent>
                    </Card>
                  );
                })
              )}
            </Stack>
          </Stack>
        </CardContent>
      </Card>

      <Grid container spacing={2}>
        <Grid item xs={12} md={3}>
          <MetricCard title="총 익스포저" value={money.format(metrics.grossExposure)} />
        </Grid>
        <Grid item xs={12} md={3}>
          <MetricCard title="총 평가금액" value={money.format(metrics.marketValue)} />
        </Grid>
        <Grid item xs={12} md={3}>
          <MetricCard title="실현 손익" value={money.format(metrics.realized)} />
        </Grid>
        <Grid item xs={12} md={3}>
          <MetricCard title="미실현 손익" value={money.format(metrics.unrealized)} />
        </Grid>
        <Grid item xs={12} md={3}>
          <MetricCard title="총 손익" value={money.format(metrics.totalPnl)} />
        </Grid>
        <Grid item xs={12} md={3}>
          <MetricCard title="일일 회전금액" value={money.format(metrics.turnover)} />
        </Grid>
        <Grid item xs={12} md={3}>
          <MetricCard title="회전한도 사용률" value={`${metrics.turnoverUsagePct.toFixed(2)}%`} />
        </Grid>
        <Grid item xs={12} md={3}>
          <MetricCard title="일일 회전한도" value={money.format(metrics.maxDailyTurnover)} />
        </Grid>
        <Grid item xs={12} md={3}>
          <MetricCard title="주문건수" value={String(metrics.orderCount)} />
        </Grid>
        <Grid item xs={12} md={3}>
          <MetricCard title="오픈주문 건수" value={String(metrics.openOrderCount)} />
        </Grid>
        <Grid item xs={12} md={3}>
          <MetricCard title="완료주문 건수" value={String(metrics.filledOrderCount)} />
        </Grid>
        <Grid item xs={12} md={3}>
          <MetricCard title="체결건수" value={String(metrics.tradeCount)} />
        </Grid>
        <Grid item xs={12} md={3}>
          <MetricCard title="포지션 수" value={String(metrics.positionCount)} />
        </Grid>
        <Grid item xs={12} md={3}>
          <MetricCard title="주문금액 한도" value={money.format(metrics.maxOrderNotional)} />
        </Grid>
      </Grid>

      <Stack spacing={1}>
        <Stack direction="row" spacing={1} alignItems="center" useFlexGap flexWrap="wrap">
          <Typography variant="subtitle1" fontWeight={700}>
            내 활동 피드
          </Typography>
          <Chip size="small" label={`${activityRows.length}건`} variant="outlined" />
        </Stack>

        {activityRows.length === 0 ? (
          <Alert severity="info">표시할 최근 활동이 없습니다.</Alert>
        ) : (
          <Grid container spacing={1.2}>
            {activityRows.map((row) => (
              <Grid key={row.activityKey ?? `${row.title}-${row.occurredAt}`} item xs={12} md={6}>
                <Card>
                  <CardContent sx={{ pb: 1 }}>
                    <Stack direction="row" spacing={1} alignItems="center" justifyContent="space-between">
                      <Typography variant="subtitle2" fontWeight={700}>
                        {row.title ?? "-"}
                      </Typography>
                      <Chip size="small" color={severityColor(row.severity)} label={row.severity ?? "INFO"} />
                    </Stack>
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 0.7 }}>
                      {row.description ?? "-"}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" sx={{ display: "block", mt: 0.6 }}>
                      {(row.actor ? `${row.actor} · ` : "") + (row.occurredAt ? new Date(row.occurredAt).toLocaleString() : "-")}
                    </Typography>
                  </CardContent>
                  <CardActions sx={{ px: 2, pb: 1.2 }}>
                    <Button size="small" variant="outlined" disabled={!row.path} onClick={() => jumpToHash(row.path)}>
                      관련 화면
                    </Button>
                  </CardActions>
                </Card>
              </Grid>
            ))}
          </Grid>
        )}
      </Stack>

      <Stack spacing={1}>
        <Stack direction="row" spacing={1} alignItems="center" useFlexGap flexWrap="wrap">
          <Typography variant="subtitle1" fontWeight={700}>
            오늘의 운영 작업
          </Typography>
          <Chip label={`오픈주문 ${Number(queueSummary.openOrderCount ?? 0)}건`} size="small" variant="outlined" />
          <Chip label={`지연주문 ${Number(queueSummary.staleOrderCount ?? 0)}건`} size="small" variant="outlined" />
          <Chip label={`치명경보 ${Number(queueSummary.criticalRiskCount ?? 0)}건`} size="small" color="error" />
        </Stack>

        {queueTasks.length === 0 ? (
          <Alert severity="success">현재 처리할 우선 작업이 없습니다.</Alert>
        ) : (
          <Grid container spacing={1.5}>
            {queueTasks.map((task, idx) => (
              <Grid key={`${task.taskKey ?? "task"}-${idx}`} item xs={12} md={6} lg={4}>
                <Card sx={{ height: "100%" }}>
                  <CardContent sx={{ pb: 1 }}>
                    <Stack direction="row" spacing={1} alignItems="center" justifyContent="space-between">
                      <Typography variant="subtitle2" fontWeight={700}>
                        {task.title ?? "작업"}
                      </Typography>
                      <Chip
                        size="small"
                        label={`${task.severity ?? "INFO"} · ${Number(task.count ?? 0)}건`}
                        color={severityColor(task.severity)}
                      />
                    </Stack>
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 0.7 }}>
                      {task.description ?? "-"}
                    </Typography>
                  </CardContent>
                  <CardActions sx={{ px: 2, pb: 1.5 }}>
                    {Boolean(task.actionEnabled) && canQuickAction(task) ? (
                      <Button
                        variant="contained"
                        color={quickActionColor(task)}
                        size="small"
                        disabled={actionLoadingKey === task.taskKey}
                        onClick={() => {
                          void handleQuickAction(task);
                        }}
                      >
                        {actionLoadingKey === task.taskKey ? "실행 중..." : "즉시 실행"}
                      </Button>
                    ) : null}
                    <Button
                      variant="outlined"
                      size="small"
                      disabled={!Boolean(task.path)}
                      onClick={() => jumpToHash(task.path)}
                    >
                      {task.actionLabel || "화면 열기"}
                    </Button>
                    {!task.actionEnabled ? <Chip size="small" label="조회 전용" variant="outlined" /> : null}
                  </CardActions>
                </Card>
              </Grid>
            ))}
          </Grid>
        )}
      </Stack>

      <Grid container spacing={2}>
        {isTrader ? (
          <>
            <Grid item xs={12} md={4}>
              <MetricCard
                title="체결 집중도(체결/주문)"
                value={metrics.orderCount <= 0 ? "0.00%" : `${((metrics.tradeCount / metrics.orderCount) * 100).toFixed(2)}%`}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <MetricCard
                title="평균 주문당 체결건"
                value={metrics.orderCount <= 0 ? "0.00" : (metrics.tradeCount / metrics.orderCount).toFixed(2)}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <MetricCard
                title="평균 주문당 노출금액"
                value={metrics.orderCount <= 0 ? money.format(0) : money.format(metrics.grossExposure / metrics.orderCount)}
              />
            </Grid>
          </>
        ) : null}

        {isRisk ? (
          <>
            <Grid item xs={12} md={4}>
              <MetricCard
                title="회전여유 한도"
                value={money.format(Math.max(0, metrics.maxDailyTurnover - metrics.turnover))}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <MetricCard
                title="주문한도 대비 회전한도"
                value={metrics.maxOrderNotional <= 0 ? "0.00x" : `${(metrics.maxDailyTurnover / metrics.maxOrderNotional).toFixed(2)}x`}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <MetricCard title="회전 한도 위험도" value={`${Math.max(0, metrics.turnoverUsagePct).toFixed(2)}%`} />
            </Grid>
          </>
        ) : null}

        {isViewer && !isTrader && !isRisk ? (
          <Grid item xs={12} md={4}>
            <MetricCard title="조회용 상태" value="READ-ONLY" />
          </Grid>
        ) : null}
      </Grid>

      <Stack spacing={1}>
        <Typography variant="subtitle1" fontWeight={700}>
          운영 경고
        </Typography>
        {[...queueAlerts.map((item) => item.message ?? "").filter(Boolean), ...alertMessages].length === 0 ? (
          <Alert severity="success">현재 주요 리스크 지표는 정상 범위입니다.</Alert>
        ) : (
          [...queueAlerts.map((item) => ({ message: item.message ?? "", severity: item.severity ?? "WARN" })), ...alertMessages.map((message) => ({ message, severity: statusLevel.label }))]
            .filter((row) => row.message)
            .map((row, idx) => (
            <Alert key={`${idx}-${row.message}`} severity={severityColor(row.severity) === "error" ? "error" : severityColor(row.severity) === "warning" ? "warning" : "info"}>
              {row.message}
            </Alert>
          ))
        )}
      </Stack>

      {(summaryQuery.isPending || riskLimitQuery.isPending || workQueueQuery.isPending || activityFeedQuery.isPending || playbookQuery.isPending) && (
        <Typography color="text.secondary" variant="body2">
          지표를 불러오는 중...
        </Typography>
      )}

      <Dialog open={resumeGuardDialog.open} onClose={closeResumeGuardDialog} fullWidth maxWidth="sm">
        <DialogTitle>거래 재개 차단 사유</DialogTitle>
        <DialogContent>
          <Stack spacing={1}>
            <Typography variant="body2" color="text.secondary">
              현재 치명 경보 {resumeGuardDialog.blockedCount}건이 남아 있어 거래 재개가 차단되었습니다.
            </Typography>
            {resumeGuardDialog.blockedMessages.length > 0 ? (
              <MuiList dense sx={{ py: 0 }}>
                {resumeGuardDialog.blockedMessages.map((message) => (
                  <ListItem key={message} disableGutters>
                    <ListItemText primaryTypographyProps={{ variant: "body2" }} primary={message} />
                  </ListItem>
                ))}
              </MuiList>
            ) : (
              <Typography variant="body2">
                코드: {resumeGuardDialog.blockedCodes.join(", ") || "-"}
              </Typography>
            )}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeResumeGuardDialog} disabled={actionLoadingKey === "resumeTradingForce"}>
            닫기
          </Button>
          <Button
            color="warning"
            variant="contained"
            onClick={() => {
              void handleForceResume();
            }}
            disabled={actionLoadingKey === "resumeTradingForce"}
          >
            {actionLoadingKey === "resumeTradingForce" ? "강제 재개 중..." : "강제 재개"}
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}
