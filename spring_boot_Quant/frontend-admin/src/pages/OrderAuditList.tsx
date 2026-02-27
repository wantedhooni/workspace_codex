import { Alert, Box, Card, CardContent, Chip, Stack, Tooltip, Typography } from "@mui/material";
import { useMemo } from "react";
import {
  Datagrid,
  FunctionField,
  List,
  NumberField,
  NumberInput,
  TextField,
  TextInput,
  useGetList,
  useListContext
} from "react-admin";
import { PortfolioSelectInput } from "../components/PortfolioSelectInput";
import { RecordDetailButton } from "../components/RecordDetailButton";
import { usePortfolioCatalog } from "../portfolio/portfolioCatalog";
import { useSelectedPortfolioId } from "../portfolio/portfolioSelection";

type OrderAuditSummaryActionCounter = {
  action: string;
  count: number;
};

type OrderAuditSummaryTransitionCounter = {
  fromStatus: string;
  toStatus: string;
  count: number;
};

type OrderAuditSummaryActorCounter = {
  actor: string;
  count: number;
};

type OrderAuditSummaryRow = {
  id: string;
  portfolioId: number;
  recentMinutes: number;
  totalCount: number;
  recentCount: number;
  distinctOrderCount: number;
  lastActedAt?: string;
  generatedAt?: string;
  actionCounters?: OrderAuditSummaryActionCounter[];
  transitionCounters?: OrderAuditSummaryTransitionCounter[];
  topActors?: OrderAuditSummaryActorCounter[];
};

const formatDateTime = (value: unknown) => {
  if (!value) return "-";
  const date = new Date(String(value));
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleString("ko-KR", { hour12: false });
};

function MetricCard({ label, value }: { label: string; value: string }) {
  return (
    <Card variant="outlined" sx={{ minWidth: 140 }}>
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

function actionChipColor(action: string): "default" | "info" | "warning" | "success" | "error" {
  if (action === "CREATE") return "info";
  if (action === "TRADE_APPLIED") return "success";
  if (action === "CANCEL") return "warning";
  if (action === "REJECT" || action === "DELETE") return "error";
  return "default";
}

function OrderAuditSummaryPanel() {
  const { filterValues, setFilters } = useListContext();
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const currentFilters = (filterValues ?? {}) as Record<string, unknown>;

  const portfolioId = Number(currentFilters.portfolioId ?? selectedPortfolioId);
  const normalizedPortfolioId = Number.isFinite(portfolioId) && portfolioId > 0 ? Math.trunc(portfolioId) : selectedPortfolioId;
  const symbolFilter = String(currentFilters.symbol ?? "").trim().toUpperCase();
  const actionFilter = String(currentFilters.action ?? "").trim().toUpperCase();
  const actorFilter = String(currentFilters.actor ?? "").trim().toLowerCase();

  const summaryFilter = useMemo(
    () => ({
      portfolioId: normalizedPortfolioId,
      symbol: symbolFilter,
      action: actionFilter,
      actor: actorFilter,
      recentMinutes: 180
    }),
    [normalizedPortfolioId, symbolFilter, actionFilter, actorFilter]
  );

  const summaryQuery = useGetList<OrderAuditSummaryRow>("orderAuditSummaries", {
    pagination: { page: 1, perPage: 1 },
    sort: { field: "generatedAt", order: "DESC" },
    filter: summaryFilter
  });
  const summary = (summaryQuery.data?.[0] ?? null) as OrderAuditSummaryRow | null;

  const applyActionFilter = (nextAction: string) => {
    const toggledAction = actionFilter === nextAction ? "" : nextAction;
    setFilters(
      {
        ...currentFilters,
        portfolioId: normalizedPortfolioId,
        action: toggledAction
      },
      undefined,
      false
    );
  };

  return (
    <Card variant="outlined" sx={{ mb: 1.5 }}>
      <CardContent sx={{ py: 1.25, "&:last-child": { pb: 1.25 } }}>
        <Stack spacing={1.2}>
          <Stack direction="row" justifyContent="space-between" alignItems="center" useFlexGap flexWrap="wrap">
            <Typography variant="subtitle1" fontWeight={700}>
              감사 요약
            </Typography>
            {summary ? (
              <Typography variant="caption" color="text.secondary">
                최근 {summary.recentMinutes}분 기준 · 마지막 반영 {formatDateTime(summary.lastActedAt)}
              </Typography>
            ) : null}
          </Stack>

          {summaryQuery.isLoading ? (
            <Typography variant="body2" color="text.secondary">
              감사로그 요약 로딩 중...
            </Typography>
          ) : null}
          {summaryQuery.error ? (
            <Alert severity="warning">감사로그 요약 조회에 실패했습니다.</Alert>
          ) : null}

          {summary ? (
            <>
              <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
                <MetricCard label="총 이벤트" value={`${summary.totalCount ?? 0}`} />
                <MetricCard label="최근 이벤트" value={`${summary.recentCount ?? 0}`} />
                <MetricCard label="주문 개수" value={`${summary.distinctOrderCount ?? 0}`} />
                <MetricCard label="필터 액션" value={actionFilter || "전체"} />
              </Stack>

              <Box>
                <Typography variant="caption" color="text.secondary">
                  액션 퀵 필터
                </Typography>
                <Stack direction="row" spacing={1} sx={{ mt: 0.4 }} useFlexGap flexWrap="wrap">
                  {(summary.actionCounters ?? []).slice(0, 8).map((item) => (
                    <Chip
                      key={item.action}
                      size="small"
                      label={`${item.action} ${item.count}`}
                      color={item.action === actionFilter ? "primary" : actionChipColor(item.action)}
                      variant={item.action === actionFilter ? "filled" : "outlined"}
                      onClick={() => applyActionFilter(item.action)}
                    />
                  ))}
                </Stack>
              </Box>

              <Box>
                <Typography variant="caption" color="text.secondary">
                  주요 상태전이
                </Typography>
                <Stack direction="row" spacing={1} sx={{ mt: 0.4 }} useFlexGap flexWrap="wrap">
                  {(summary.transitionCounters ?? []).slice(0, 6).map((item) => (
                    <Chip
                      key={`${item.fromStatus}-${item.toStatus}`}
                      size="small"
                      variant="outlined"
                      label={`${item.fromStatus}→${item.toStatus} (${item.count})`}
                    />
                  ))}
                </Stack>
              </Box>

              <Box>
                <Typography variant="caption" color="text.secondary">
                  주요 행위자
                </Typography>
                <Stack direction="row" spacing={1} sx={{ mt: 0.4 }} useFlexGap flexWrap="wrap">
                  {(summary.topActors ?? []).slice(0, 6).map((item) => (
                    <Chip key={item.actor} size="small" label={`${item.actor} (${item.count})`} />
                  ))}
                </Stack>
              </Box>
            </>
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  );
}

export function OrderAuditList() {
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const { formatPortfolioLabel } = usePortfolioCatalog();

  return (
    <List
      title="주문 감사로그"
      perPage={25}
      filters={[
        <PortfolioSelectInput key="portfolioId" source="portfolioId" label="포트폴리오" alwaysOn />,
        <NumberInput key="orderId" source="orderId" label="Order ID" />,
        <TextInput key="symbol" source="symbol" label="종목" />,
        <TextInput key="action" source="action" label="액션" />,
        <TextInput key="actor" source="actor" label="행위자" />
      ]}
      filter={{ portfolioId: selectedPortfolioId }}
    >
      <OrderAuditSummaryPanel />
      <Datagrid bulkActionButtons={false} rowClick={false}>
        <NumberField source="auditId" label="Audit ID" />
        <NumberField source="orderId" label="Order ID" />
        <FunctionField label="포트폴리오" render={(record) => formatPortfolioLabel(record.portfolioId)} />
        <TextField source="symbol" label="종목" />
        <FunctionField
          label="액션"
          render={(record) => {
            const action = String(record.action ?? "UNKNOWN").toUpperCase();
            return <Chip size="small" color={actionChipColor(action)} label={action} variant="outlined" />;
          }}
        />
        <FunctionField
          label="상태전이"
          render={(record) => {
            const fromStatus = String(record.fromStatus ?? "-");
            const toStatus = String(record.toStatus ?? "-");
            return `${fromStatus} → ${toStatus}`;
          }}
        />
        <FunctionField
          label="사유"
          render={(record) => {
            const reason = String(record.reason ?? "-");
            return (
              <Tooltip title={reason}>
                <Box sx={{ maxWidth: 260, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{reason}</Box>
              </Tooltip>
            );
          }}
        />
        <TextField source="actor" label="행위자" />
        <FunctionField label="수행시각" render={(record) => formatDateTime(record.actedAt)} />
        <FunctionField
          label="상세"
          render={() => (
            <RecordDetailButton
              title="주문 감사로그 상세"
              fields={[
                { source: "auditId", label: "Audit ID" },
                { source: "orderId", label: "Order ID" },
                { source: "portfolioId", label: "Portfolio" },
                { source: "symbol", label: "종목" },
                { source: "action", label: "액션" },
                { source: "fromStatus", label: "이전상태" },
                { source: "toStatus", label: "변경상태" },
                { source: "reason", label: "사유" },
                { source: "actor", label: "행위자" },
                {
                  source: "actedAt",
                  label: "수행시각",
                  render: (value) => formatDateTime(value)
                }
              ]}
            />
          )}
        />
      </Datagrid>
    </List>
  );
}
