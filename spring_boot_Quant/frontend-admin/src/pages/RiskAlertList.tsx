import { Alert, Box, Card, CardContent, Chip, Stack, Tooltip, Typography } from "@mui/material";
import { useMemo } from "react";
import {
  Button,
  Datagrid,
  FunctionField,
  List,
  NumberInput,
  NumberField,
  SelectInput,
  TextField,
  TextInput,
  useDataProvider,
  useGetList,
  useListContext,
  useNotify,
  usePermissions,
  useRecordContext,
  useRefresh
} from "react-admin";
import { PortfolioSelectInput } from "../components/PortfolioSelectInput";
import { RecordDetailButton } from "../components/RecordDetailButton";
import type { AppPermissions } from "../providers/authProvider";
import { usePortfolioCatalog } from "../portfolio/portfolioCatalog";
import { useSelectedPortfolioId } from "../portfolio/portfolioSelection";

const formatDateTime = (value: unknown) => {
  if (!value) {
    return "-";
  }
  const date = new Date(String(value));
  if (Number.isNaN(date.getTime())) {
    return String(value);
  }
  return date.toLocaleString("ko-KR", { hour12: false });
};

const formatMinutes = (value: unknown) => {
  const minutes = Number(value ?? 0);
  if (!Number.isFinite(minutes) || minutes < 0) {
    return "-";
  }
  const h = Math.floor(minutes / 60);
  const m = Math.floor(minutes % 60);
  if (h <= 0) return `${m}m`;
  return `${h}h ${m}m`;
};

type RiskAlertOverviewRow = {
  id: string;
  portfolioId: number;
  totalCount: number;
  criticalCount: number;
  warnCount: number;
  infoCount: number;
  unacknowledgedCount: number;
  openCount: number;
  inProgressCount: number;
  resolvedCount: number;
  slaBreachedCount: number;
  oldestOpenAgeMinutes: number;
  avgAckMinutes?: number;
  avgResolveMinutes?: number;
  generatedAt?: string;
};

function MetricCard({ label, value }: { label: string; value: string }) {
  return (
    <Card variant="outlined" sx={{ minWidth: 130 }}>
      <CardContent sx={{ py: 1, "&:last-child": { pb: 1 } }}>
        <Typography variant="caption" color="text.secondary">
          {label}
        </Typography>
        <Typography variant="h6" fontWeight={700}>
          {value}
        </Typography>
      </CardContent>
    </Card>
  );
}

function RiskAlertOverviewPanel() {
  const { filterValues, setFilters } = useListContext();
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const currentFilters = (filterValues ?? {}) as Record<string, unknown>;
  const portfolioIdValue = Number(currentFilters.portfolioId ?? selectedPortfolioId);
  const portfolioId = Number.isFinite(portfolioIdValue) && portfolioIdValue > 0 ? Math.trunc(portfolioIdValue) : selectedPortfolioId;

  const overviewFilter = useMemo(() => ({ portfolioId }), [portfolioId]);
  const overviewQuery = useGetList<RiskAlertOverviewRow>("riskAlertOverviews", {
    pagination: { page: 1, perPage: 1 },
    sort: { field: "generatedAt", order: "DESC" },
    filter: overviewFilter
  });
  const overview = (overviewQuery.data?.[0] ?? null) as RiskAlertOverviewRow | null;

  const setQuickFilter = (next: Record<string, unknown>) => {
    setFilters(
      {
        ...currentFilters,
        portfolioId,
        ...next
      },
      undefined,
      false
    );
  };

  return (
    <Card variant="outlined" sx={{ mb: 1.5 }}>
      <CardContent sx={{ py: 1.25, "&:last-child": { pb: 1.25 } }}>
        <Stack spacing={1.1}>
          <Stack direction="row" justifyContent="space-between" alignItems="center" useFlexGap flexWrap="wrap">
            <Typography variant="subtitle1" fontWeight={700}>
              리스크 운영 오버뷰
            </Typography>
            {overview ? (
              <Typography variant="caption" color="text.secondary">
                생성시각 {formatDateTime(overview.generatedAt)}
              </Typography>
            ) : null}
          </Stack>

          {overviewQuery.isLoading ? (
            <Typography variant="body2" color="text.secondary">
              리스크 오버뷰 로딩 중...
            </Typography>
          ) : null}
          {overviewQuery.error ? <Alert severity="warning">리스크 오버뷰 조회에 실패했습니다.</Alert> : null}

          {overview ? (
            <>
              <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
                <MetricCard label="전체 경보" value={`${overview.totalCount ?? 0}`} />
                <MetricCard label="Critical" value={`${overview.criticalCount ?? 0}`} />
                <MetricCard label="Warn" value={`${overview.warnCount ?? 0}`} />
                <MetricCard label="미확인" value={`${overview.unacknowledgedCount ?? 0}`} />
                <MetricCard label="SLA 위반" value={`${overview.slaBreachedCount ?? 0}`} />
                <MetricCard label="최장 오픈" value={formatMinutes(overview.oldestOpenAgeMinutes)} />
                <MetricCard label="평균 ACK" value={formatMinutes(overview.avgAckMinutes ?? null)} />
                <MetricCard label="평균 해결" value={formatMinutes(overview.avgResolveMinutes ?? null)} />
              </Stack>

              <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
                <Chip size="small" label={`OPEN ${overview.openCount ?? 0}`} variant="outlined" />
                <Chip size="small" label={`IN_PROGRESS ${overview.inProgressCount ?? 0}`} variant="outlined" />
                <Chip size="small" label={`RESOLVED ${overview.resolvedCount ?? 0}`} variant="outlined" />
                <Button
                  label="Critical만"
                  onClick={() => setQuickFilter({ severity: "CRITICAL", workflowStatus: "", acknowledged: "", slaBreached: "" })}
                />
                <Button
                  label="SLA 위반만"
                  onClick={() => setQuickFilter({ severity: "", workflowStatus: "", slaBreached: "true" })}
                />
                <Button
                  label="미확인만"
                  onClick={() => setQuickFilter({ severity: "", workflowStatus: "", acknowledged: "false", slaBreached: "" })}
                />
                <Button label="필터 초기화" onClick={() => setQuickFilter({ severity: "", workflowStatus: "", acknowledged: "", slaBreached: "", minPriorityScore: "" })} />
              </Stack>

              {(overview.slaBreachedCount ?? 0) > 0 ? (
                <Alert severity="error">SLA 위반 경보가 존재합니다. 우선순위가 높은 경보를 먼저 처리하세요.</Alert>
              ) : null}
            </>
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  );
}

function RiskAlertAckButtons() {
  const record = useRecordContext<Record<string, unknown>>();
  const dp = useDataProvider();
  const notify = useNotify();
  const refresh = useRefresh();
  const { permissions } = usePermissions<AppPermissions>();

  if (!record) {
    return null;
  }

  const severity = String(record.severity ?? "INFO").toUpperCase();
  const canUpdate = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.riskAlerts?.canUpdate);
  const acknowledged = Boolean(record.acknowledged);
  const workflowStatus = String(record.workflowStatus ?? "OPEN").toUpperCase();
  const alertKey = String(record.alertKey ?? "");
  const portfolioId = Number(record.portfolioId ?? 0);
  const disabled = !canUpdate || severity === "INFO" || !alertKey || !Number.isFinite(portfolioId) || portfolioId <= 0;

  const doUpdate = async (action: "ack" | "unack") => {
    try {
      await dp.update("riskAlerts", {
        id: alertKey,
        data: {
          portfolioId,
          alertKey,
          note: action === "ack" ? "operator acknowledged from risk alert list" : ""
        },
        previousData: record,
        meta: { action }
      });
      notify(action === "ack" ? "경보 확인 처리 완료" : "경보 확인해제 완료", { type: "info" });
      refresh();
    } catch (error) {
      notify(error instanceof Error ? error.message : "경보 상태 변경 실패", { type: "warning" });
    }
  };

  const doWorkflow = async (nextStatus: "OPEN" | "IN_PROGRESS" | "RESOLVED", note: string) => {
    const assignee = nextStatus === "IN_PROGRESS" ? String(record.assignee ?? "") : "";
    try {
      await dp.update("riskAlerts", {
        id: alertKey,
        data: {
          portfolioId,
          alertKey,
          workflowStatus: nextStatus,
          note,
          assignee
        },
        previousData: record,
        meta: { action: "workflow" }
      });
      notify(`경보 상태가 ${nextStatus}로 변경되었습니다.`, { type: "info" });
      refresh();
    } catch (error) {
      notify(error instanceof Error ? error.message : "경보 워크플로 변경 실패", { type: "warning" });
    }
  };

  return (
    <Stack direction="row" spacing={1}>
      {acknowledged ? (
        <Button label="확인해제" disabled={disabled} onClick={() => doUpdate("unack")} />
      ) : (
        <Button label="확인" disabled={disabled} onClick={() => doUpdate("ack")} />
      )}
      {acknowledged ? (
        <>
          {workflowStatus !== "IN_PROGRESS" ? (
            <Button label="진행중" disabled={disabled} onClick={() => doWorkflow("IN_PROGRESS", "triage in progress")} />
          ) : null}
          {workflowStatus !== "RESOLVED" ? (
            <Button label="해결" disabled={disabled} onClick={() => doWorkflow("RESOLVED", "resolved from risk alert list")} />
          ) : null}
          {workflowStatus !== "OPEN" ? (
            <Button label="재오픈" disabled={disabled} onClick={() => doWorkflow("OPEN", "re-opened from risk alert list")} />
          ) : null}
        </>
      ) : null}
    </Stack>
  );
}

export function RiskAlertList() {
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const { formatPortfolioLabel } = usePortfolioCatalog();

  return (
    <List
      title="리스크 경보"
      perPage={25}
      filters={[
        <PortfolioSelectInput key="portfolioId" source="portfolioId" label="포트폴리오" alwaysOn />,
        <SelectInput
          key="severity"
          source="severity"
          label="심각도"
          emptyText="전체"
          choices={[
            { id: "CRITICAL", name: "CRITICAL" },
            { id: "WARN", name: "WARN" },
            { id: "INFO", name: "INFO" }
          ]}
        />,
        <SelectInput
          key="workflowStatus"
          source="workflowStatus"
          label="워크플로 상태"
          emptyText="전체"
          choices={[
            { id: "OPEN", name: "OPEN" },
            { id: "IN_PROGRESS", name: "IN_PROGRESS" },
            { id: "RESOLVED", name: "RESOLVED" }
          ]}
        />,
        <SelectInput
          key="acknowledged"
          source="acknowledged"
          label="확인상태"
          emptyText="전체"
          choices={[
            { id: "true", name: "ACK" },
            { id: "false", name: "UNACK" }
          ]}
        />,
        <SelectInput
          key="slaBreached"
          source="slaBreached"
          label="SLA"
          emptyText="전체"
          choices={[
            { id: "true", name: "BREACHED" },
            { id: "false", name: "ON_TRACK" }
          ]}
        />,
        <TextInput key="code" source="code" label="코드" />,
        <NumberInput key="minPriorityScore" source="minPriorityScore" label="우선순위 >= " min={0} />
      ]}
      filter={{ portfolioId: selectedPortfolioId }}
    >
      <RiskAlertOverviewPanel />
      <Datagrid bulkActionButtons={false} rowClick={false}>
        <FunctionField
          label="심각도"
          render={(record) => {
            const severity = String(record.severity ?? "INFO").toUpperCase();
            const color = severity === "CRITICAL" ? "error" : severity === "WARN" ? "warning" : "default";
            return <Chip size="small" color={color} label={severity} variant={severity === "INFO" ? "outlined" : "filled"} />;
          }}
        />
        <NumberField source="priorityScore" label="우선순위" />
        <TextField source="code" label="코드" />
        <TextField source="message" label="메시지" />
        <FunctionField
          label="워크플로"
          render={(record) => {
            const status = String(record.workflowStatus ?? "OPEN").toUpperCase();
            const color = status === "RESOLVED" ? "success" : status === "IN_PROGRESS" ? "warning" : "default";
            return <Chip size="small" color={color} label={status} variant={status === "OPEN" ? "outlined" : "filled"} />;
          }}
        />
        <FunctionField
          label="확인상태"
          render={(record) => (
            <Chip
              size="small"
              color={Boolean(record.acknowledged) ? "success" : "default"}
              label={Boolean(record.acknowledged) ? "ACK" : "UNACK"}
              variant={Boolean(record.acknowledged) ? "filled" : "outlined"}
            />
          )}
        />
        <FunctionField
          label="SLA"
          render={(record) => {
            const breached = Boolean(record.slaBreached);
            const age = formatMinutes(record.ageMinutes);
            const target = formatMinutes(record.slaTargetMinutes);
            return (
              <Tooltip title={`경과 ${age} / 목표 ${target}`}>
                <Chip size="small" color={breached ? "error" : "success"} label={breached ? "BREACHED" : "ON_TRACK"} variant="outlined" />
              </Tooltip>
            );
          }}
        />
        <FunctionField
          label="지표"
          render={(record) =>
            `${String(record.metricName ?? "-")} (${String(record.metricValue ?? "-")} / ${String(record.thresholdValue ?? "-")})`
          }
        />
        <FunctionField label="발생 경과" render={(record) => formatMinutes(record.ageMinutes)} />
        <FunctionField label="발생시각" render={(record) => formatDateTime(record.occurredAt)} />
        <TextField source="assignee" label="담당자" />
        <TextField source="acknowledgedBy" label="확인자" />
        <FunctionField label="확인시각" render={(record) => formatDateTime(record.acknowledgedAt)} />
        <TextField source="resolvedBy" label="해결자" />
        <FunctionField label="해결시각" render={(record) => formatDateTime(record.resolvedAt)} />
        <FunctionField label="포트폴리오" render={(record) => formatPortfolioLabel(record.portfolioId)} />
        <TextField source="alertKey" label="Alert Key" />
        <FunctionField
          label="상세"
          render={() => (
            <RecordDetailButton
              title="리스크 경보 상세"
              fields={[
                { source: "alertKey", label: "Alert Key" },
                { source: "portfolioId", label: "Portfolio" },
                { source: "severity", label: "심각도" },
                { source: "code", label: "코드" },
                { source: "message", label: "메시지" },
                { source: "metricName", label: "지표" },
                { source: "metricValue", label: "값" },
                { source: "thresholdValue", label: "임계치" },
                { source: "occurredAt", label: "발생시각" },
                { source: "acknowledged", label: "확인여부" },
                { source: "acknowledgementNote", label: "확인메모" },
                { source: "acknowledgedBy", label: "확인자" },
                { source: "acknowledgedAt", label: "확인시각" },
                { source: "workflowStatus", label: "워크플로 상태" },
                { source: "assignee", label: "담당자" },
                { source: "resolvedBy", label: "해결자" },
                { source: "resolvedAt", label: "해결시각" },
                { source: "workflowUpdatedBy", label: "최종갱신자" },
                { source: "workflowUpdatedAt", label: "최종갱신시각" },
                { source: "ageMinutes", label: "경과(분)" },
                { source: "slaTargetMinutes", label: "SLA 목표(분)" },
                { source: "slaBreached", label: "SLA 위반" },
                { source: "priorityScore", label: "우선순위 점수" }
              ]}
            />
          )}
        />
        <FunctionField label="액션" render={() => <RiskAlertAckButtons />} />
      </Datagrid>
    </List>
  );
}
