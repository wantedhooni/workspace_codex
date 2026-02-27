import { Alert, Box, Card, CardContent, Chip, Stack, Tooltip, Typography } from "@mui/material";
import { useMemo } from "react";
import { Datagrid, FunctionField, List, NumberField, useGetList, useListContext } from "react-admin";
import { PortfolioSelectInput } from "../components/PortfolioSelectInput";
import { RecordDetailButton } from "../components/RecordDetailButton";
import { usePortfolioCatalog } from "../portfolio/portfolioCatalog";
import { useSelectedPortfolioId } from "../portfolio/portfolioSelection";

const money = new Intl.NumberFormat("ko-KR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const pct = new Intl.NumberFormat("ko-KR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });

type TopExposure = {
  symbol: string;
  quantity: number;
  marketValue: number;
  grossExposureWeightPct: number;
  totalPnl: number;
};

type PortfolioInsightRow = {
  id: string;
  portfolioId: number;
  healthScore: number;
  healthStatus: string;
  pnlMarginPct: number;
  turnoverUsagePct: number;
  openOrderRatioPct: number;
  orderPressurePct: number;
  criticalAlertCount: number;
  warnAlertCount: number;
  tradingEnabled: boolean;
  topConcentrationSymbol?: string;
  topConcentrationWeightPct: number;
  generatedAt?: string;
  topExposures?: TopExposure[];
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

function healthChipColor(status: string): "success" | "warning" | "error" | "default" {
  if (status === "HEALTHY") return "success";
  if (status === "WARN") return "warning";
  if (status === "CRITICAL") return "error";
  return "default";
}

function PortfolioSummaryInsightPanel() {
  const { filterValues } = useListContext();
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const rawPortfolioId = Number((filterValues as Record<string, unknown> | undefined)?.portfolioId ?? selectedPortfolioId);
  const portfolioId = Number.isFinite(rawPortfolioId) && rawPortfolioId > 0 ? Math.trunc(rawPortfolioId) : selectedPortfolioId;

  const insightFilter = useMemo(() => ({ portfolioId }), [portfolioId]);
  const insightQuery = useGetList<PortfolioInsightRow>("portfolioSummaryInsights", {
    pagination: { page: 1, perPage: 1 },
    sort: { field: "healthScore", order: "DESC" },
    filter: insightFilter
  });
  const insight = (insightQuery.data?.[0] ?? null) as PortfolioInsightRow | null;

  return (
    <Card variant="outlined" sx={{ mb: 1.5 }}>
      <CardContent sx={{ py: 1.25, "&:last-child": { pb: 1.25 } }}>
        <Stack spacing={1.2}>
          <Stack direction="row" justifyContent="space-between" alignItems="center" useFlexGap flexWrap="wrap">
            <Typography variant="subtitle1" fontWeight={700}>
              포트폴리오 운영 인사이트
            </Typography>
            {insight ? (
              <Typography variant="caption" color="text.secondary">
                생성시각 {formatDateTime(insight.generatedAt)}
              </Typography>
            ) : null}
          </Stack>

          {insightQuery.isLoading ? (
            <Typography variant="body2" color="text.secondary">
              포트폴리오 인사이트 로딩 중...
            </Typography>
          ) : null}
          {insightQuery.error ? (
            <Alert severity="warning">포트폴리오 인사이트 조회에 실패했습니다.</Alert>
          ) : null}

          {insight ? (
            <>
              <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
                <MetricCard label="건강 점수" value={`${insight.healthScore ?? 0}`} />
                <MetricCard label="PnL 마진" value={`${pct.format(Number(insight.pnlMarginPct ?? 0))}%`} />
                <MetricCard label="회전 사용률" value={`${pct.format(Number(insight.turnoverUsagePct ?? 0))}%`} />
                <MetricCard label="오픈 주문 비율" value={`${pct.format(Number(insight.openOrderRatioPct ?? 0))}%`} />
                <MetricCard label="주요 집중" value={`${insight.topConcentrationSymbol ?? "-"} ${pct.format(Number(insight.topConcentrationWeightPct ?? 0))}%`} />
              </Stack>

              <Stack direction="row" spacing={1} alignItems="center" useFlexGap flexWrap="wrap">
                <Chip label={insight.healthStatus ?? "UNKNOWN"} color={healthChipColor(String(insight.healthStatus ?? ""))} />
                <Chip label={`Critical ${insight.criticalAlertCount ?? 0}`} color={(insight.criticalAlertCount ?? 0) > 0 ? "error" : "default"} variant="outlined" />
                <Chip label={`Warn ${insight.warnAlertCount ?? 0}`} color={(insight.warnAlertCount ?? 0) > 0 ? "warning" : "default"} variant="outlined" />
                <Chip label={`거래상태 ${insight.tradingEnabled ? "ENABLED" : "HALTED"}`} color={insight.tradingEnabled ? "success" : "error"} variant="outlined" />
              </Stack>

              {!insight.tradingEnabled || (insight.criticalAlertCount ?? 0) > 0 ? (
                <Alert severity="error">
                  거래 상태 또는 치명 경보를 우선 확인하세요. Kill Switch/리스크 경보 해제 후 주문 집행을 권장합니다.
                </Alert>
              ) : null}

              {(insight.topExposures ?? []).length > 0 ? (
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    상위 익스포저
                  </Typography>
                  <Stack direction="row" spacing={1} sx={{ mt: 0.4 }} useFlexGap flexWrap="wrap">
                    {(insight.topExposures ?? []).map((item) => (
                      <Tooltip
                        key={item.symbol}
                        title={`평가금액 ${money.format(Number(item.marketValue ?? 0))}, PnL ${money.format(Number(item.totalPnl ?? 0))}`}
                      >
                        <Chip label={`${item.symbol} ${pct.format(Number(item.grossExposureWeightPct ?? 0))}%`} size="small" variant="outlined" />
                      </Tooltip>
                    ))}
                  </Stack>
                </Box>
              ) : null}
            </>
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  );
}

export function PortfolioSummaryList() {
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const { formatPortfolioLabel } = usePortfolioCatalog();

  return (
    <List
      title="포트폴리오 요약"
      perPage={25}
      filters={[<PortfolioSelectInput key="portfolioId" source="portfolioId" label="포트폴리오" alwaysOn />]}
      filter={{ portfolioId: selectedPortfolioId }}
    >
      <PortfolioSummaryInsightPanel />
      <Datagrid bulkActionButtons={false} rowClick={false}>
        <FunctionField label="포트폴리오" render={(record) => formatPortfolioLabel(record.portfolioId)} />
        <FunctionField
          label="총 손익"
          render={(record) => {
            const totalPnl = Number(record.totalPnl ?? 0);
            const color = totalPnl >= 0 ? "success.main" : "error.main";
            return <Box sx={{ color, fontWeight: 700 }}>{money.format(totalPnl)}</Box>;
          }}
        />
        <FunctionField
          label="PnL 마진(%)"
          render={(record) => {
            const marketValue = Math.abs(Number(record.marketValue ?? 0));
            const totalPnl = Number(record.totalPnl ?? 0);
            if (marketValue <= 0) return "0.00%";
            return `${((totalPnl / marketValue) * 100).toFixed(2)}%`;
          }}
        />
        <FunctionField
          label="회전률"
          render={(record) => {
            const usage = Number(record.turnoverUsagePct ?? 0);
            const color: "default" | "success" | "warning" | "error" = usage >= 95 ? "error" : usage >= 80 ? "warning" : "success";
            return <Chip size="small" label={`${pct.format(usage)}%`} color={color} variant="outlined" />;
          }}
        />
        <FunctionField
          label="오픈 주문"
          render={(record) => {
            const open = Number(record.openOrderCount ?? 0);
            const filled = Number(record.filledOrderCount ?? 0);
            const total = open + filled;
            const ratio = total > 0 ? (open / total) * 100 : 0;
            return `${open} (${ratio.toFixed(1)}%)`;
          }}
        />
        <NumberField source="positionCount" label="포지션 수" />
        <NumberField source="tradeCount" label="체결 수" />
        <NumberField source="dailyTurnover" label="일일 회전금액" />
        <FunctionField
          label="상세"
          render={() => (
            <RecordDetailButton
              title="포트폴리오 요약 상세"
              summaryItems={[
                {
                  label: "총 PnL 마진",
                  value: (record) => {
                    const marketValue = Math.abs(Number(record.marketValue ?? 0));
                    const totalPnl = Number(record.totalPnl ?? 0);
                    if (marketValue <= 0) return "0.00%";
                    return `${((totalPnl / marketValue) * 100).toFixed(2)}%`;
                  }
                },
                {
                  label: "오픈주문 비율",
                  value: (record) => {
                    const open = Number(record.openOrderCount ?? 0);
                    const done = Number(record.filledOrderCount ?? 0);
                    const total = open + done;
                    if (total <= 0) return "0.00%";
                    return `${((open / total) * 100).toFixed(2)}%`;
                  }
                },
                {
                  label: "일일 회전금액",
                  value: (record) => money.format(Number(record.dailyTurnover ?? 0))
                }
              ]}
              fields={[
                { source: "portfolioId", label: "Portfolio" },
                { source: "grossExposure", label: "총 익스포저" },
                { source: "marketValue", label: "평가금액" },
                { source: "realizedPnl", label: "실현손익" },
                { source: "unrealizedPnl", label: "미실현손익" },
                { source: "totalPnl", label: "총 손익" },
                { source: "dailyTurnover", label: "일일 회전금액" },
                { source: "turnoverUsagePct", label: "회전 한도 사용률(%)" },
                { source: "openOrderCount", label: "오픈주문 수" },
                { source: "filledOrderCount", label: "완료주문 수" },
                { source: "tradeCount", label: "체결 수" },
                { source: "positionCount", label: "포지션 수" }
              ]}
            />
          )}
        />
      </Datagrid>
    </List>
  );
}
