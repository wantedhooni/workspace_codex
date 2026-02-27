import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogContent,
  DialogTitle,
  Grid,
  Stack,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Tabs,
  Typography
} from "@mui/material";
import { useEffect, useMemo, useState } from "react";
import { useDataProvider, useRecordContext } from "react-admin";

type TradeItem = {
  tradeId: number;
  quantity: number;
  price: number;
  notional: number;
  fee: number;
  slippage: number;
  netCashFlow: number;
  tradedAt: string;
};

type AuditItem = {
  auditId: number;
  action: string;
  fromStatus: string | null;
  toStatus: string | null;
  reason: string | null;
  actor: string;
  actedAt: string;
};

type RiskItem = {
  severity: string;
  code: string;
  message: string;
  metricName: string;
  metricValue: number;
  thresholdValue: number;
  occurredAt: string;
};

type OrderInsight = {
  orderId: number;
  portfolioId: number;
  symbol: string;
  side: string;
  orderType: string;
  timeInForce: string;
  status: string;
  quantity: number;
  filledQuantity: number;
  remainingQuantity: number;
  fillRatePct: number;
  requestedNotional: number;
  executedNotional: number;
  averageExecutionPrice: number;
  totalFee: number;
  totalSlippage: number;
  netCashFlow: number;
  staleMinutes: number;
  cancelable: boolean;
  rejectable: boolean;
  decisionReason: string | null;
  createdAt: string;
  decidedAt: string | null;
  trades: TradeItem[];
  audits: AuditItem[];
  riskAlerts: RiskItem[];
};

type OrderRow = {
  orderId?: number;
};

const money = new Intl.NumberFormat("en-US", { style: "currency", currency: "USD", maximumFractionDigits: 2 });
const number = new Intl.NumberFormat("ko-KR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });

function formatMoney(value: number | null | undefined): string {
  if (value === null || value === undefined || Number.isNaN(Number(value))) {
    return "-";
  }
  return money.format(Number(value));
}

function formatNumber(value: number | null | undefined): string {
  if (value === null || value === undefined || Number.isNaN(Number(value))) {
    return "-";
  }
  return number.format(Number(value));
}

function formatTimestamp(value: string | null | undefined): string {
  if (!value) {
    return "-";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString();
}

function statusColor(status: string): "default" | "success" | "warning" | "error" | "info" {
  if (status === "FILLED") return "success";
  if (status === "PARTIAL") return "warning";
  if (status === "REJECTED") return "error";
  if (status === "CANCELED") return "default";
  return "info";
}

function severityColor(severity: string): "default" | "success" | "warning" | "error" | "info" {
  if (severity === "CRITICAL") return "error";
  if (severity === "WARN") return "warning";
  if (severity === "INFO") return "info";
  return "default";
}

type StatCardProps = {
  label: string;
  value: string;
  tone?: "default" | "good" | "warn" | "bad";
};

function StatCard({ label, value, tone = "default" }: StatCardProps) {
  const borderColor =
    tone === "good" ? "success.light" : tone === "warn" ? "warning.light" : tone === "bad" ? "error.light" : "divider";

  return (
    <Box sx={{ p: 1.5, border: 1, borderColor, borderRadius: 1.5, backgroundColor: "background.paper" }}>
      <Typography variant="caption" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="body1" fontWeight={700}>
        {value}
      </Typography>
    </Box>
  );
}

export function OrderInsightButton() {
  const record = useRecordContext<OrderRow>();
  const dataProvider = useDataProvider();
  const [open, setOpen] = useState(false);
  const [tabIndex, setTabIndex] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [insight, setInsight] = useState<OrderInsight | null>(null);

  const orderId = Number(record?.orderId ?? 0);

  const loadInsight = async () => {
    if (!Number.isFinite(orderId) || orderId <= 0) {
      setError("유효한 주문 ID가 없습니다.");
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const res = await dataProvider.getOne("orderInsights", {
        id: orderId,
        meta: { staleMinutes: 30 }
      } as never);
      setInsight(res.data as unknown as OrderInsight);
    } catch (e) {
      setInsight(null);
      setError(e instanceof Error ? e.message : "주문 인사이트 조회에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!open) {
      return;
    }
    void loadInsight();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, orderId]);

  const fillRateTone: StatCardProps["tone"] = useMemo(() => {
    const value = Number(insight?.fillRatePct ?? 0);
    if (value >= 95) return "good";
    if (value >= 70) return "warn";
    return "bad";
  }, [insight]);

  if (!record) {
    return null;
  }

  return (
    <>
      <Button
        variant="outlined"
        size="small"
        onClick={() => {
          setTabIndex(0);
          setOpen(true);
        }}
      >
        인사이트
      </Button>

      <Dialog
        open={open}
        onClose={() => {
          setOpen(false);
          setTabIndex(0);
        }}
        fullWidth
        maxWidth="lg"
      >
        <DialogTitle>
          {insight ? (
            <Stack direction="row" spacing={1} alignItems="center">
              <Typography variant="h6">{`Order #${insight.orderId} · ${insight.symbol}`}</Typography>
              <Chip size="small" label={insight.status} color={statusColor(insight.status)} />
            </Stack>
          ) : (
            "주문 인사이트"
          )}
        </DialogTitle>
        <DialogContent>
          {loading ? (
            <Stack direction="row" alignItems="center" spacing={1} sx={{ py: 2 }}>
              <CircularProgress size={20} />
              <Typography variant="body2">인사이트를 불러오는 중...</Typography>
            </Stack>
          ) : null}

          {error ? (
            <Alert severity="error" sx={{ mb: 1 }}>
              {error}
            </Alert>
          ) : null}

          {!loading && !error && insight ? (
            <>
              <Tabs value={tabIndex} onChange={(_, value: number) => setTabIndex(value)}>
                <Tab label="요약" />
                <Tab label={`체결 (${insight.trades.length})`} />
                <Tab label={`감사 (${insight.audits.length})`} />
                <Tab label={`리스크 (${insight.riskAlerts.length})`} />
              </Tabs>

              {tabIndex === 0 ? (
                <Stack spacing={1.5} sx={{ mt: 1.5 }}>
                  <Grid container spacing={1.2}>
                    <Grid item xs={12} md={3}>
                      <StatCard label="체결률" value={`${formatNumber(insight.fillRatePct)}%`} tone={fillRateTone} />
                    </Grid>
                    <Grid item xs={12} md={3}>
                      <StatCard label="요청 금액" value={formatMoney(insight.requestedNotional)} />
                    </Grid>
                    <Grid item xs={12} md={3}>
                      <StatCard label="체결 금액" value={formatMoney(insight.executedNotional)} />
                    </Grid>
                    <Grid item xs={12} md={3}>
                      <StatCard label="오픈 경과(분)" value={`${Math.max(0, Number(insight.staleMinutes ?? 0))}`} />
                    </Grid>
                  </Grid>

                  <Table size="small">
                    <TableBody>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 700, width: 180 }}>포트폴리오</TableCell>
                        <TableCell>{insight.portfolioId}</TableCell>
                        <TableCell sx={{ fontWeight: 700, width: 180 }}>매수/매도</TableCell>
                        <TableCell>{insight.side}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 700 }}>주문유형 / TIF</TableCell>
                        <TableCell>{`${insight.orderType} / ${insight.timeInForce}`}</TableCell>
                        <TableCell sx={{ fontWeight: 700 }}>수량(주문/체결/잔여)</TableCell>
                        <TableCell>{`${formatNumber(insight.quantity)} / ${formatNumber(insight.filledQuantity)} / ${formatNumber(insight.remainingQuantity)}`}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 700 }}>평균체결가</TableCell>
                        <TableCell>{formatMoney(insight.averageExecutionPrice)}</TableCell>
                        <TableCell sx={{ fontWeight: 700 }}>수수료 / 슬리피지</TableCell>
                        <TableCell>{`${formatMoney(insight.totalFee)} / ${formatMoney(insight.totalSlippage)}`}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 700 }}>순현금흐름</TableCell>
                        <TableCell>{formatMoney(insight.netCashFlow)}</TableCell>
                        <TableCell sx={{ fontWeight: 700 }}>가능 액션</TableCell>
                        <TableCell>{`취소=${insight.cancelable ? "Y" : "N"}, 거부=${insight.rejectable ? "Y" : "N"}`}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 700 }}>결정사유</TableCell>
                        <TableCell>{insight.decisionReason || "-"}</TableCell>
                        <TableCell sx={{ fontWeight: 700 }}>시각</TableCell>
                        <TableCell>{`${formatTimestamp(insight.createdAt)} / ${formatTimestamp(insight.decidedAt)}`}</TableCell>
                      </TableRow>
                    </TableBody>
                  </Table>
                </Stack>
              ) : null}

              {tabIndex === 1 ? (
                insight.trades.length > 0 ? (
                  <Table size="small" sx={{ mt: 1.5 }}>
                    <TableHead>
                      <TableRow>
                        <TableCell>Trade ID</TableCell>
                        <TableCell>수량</TableCell>
                        <TableCell>가격</TableCell>
                        <TableCell>금액</TableCell>
                        <TableCell>수수료</TableCell>
                        <TableCell>슬리피지</TableCell>
                        <TableCell>순현금흐름</TableCell>
                        <TableCell>시각</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {insight.trades.map((trade) => (
                        <TableRow key={trade.tradeId}>
                          <TableCell>{trade.tradeId}</TableCell>
                          <TableCell>{formatNumber(trade.quantity)}</TableCell>
                          <TableCell>{formatMoney(trade.price)}</TableCell>
                          <TableCell>{formatMoney(trade.notional)}</TableCell>
                          <TableCell>{formatMoney(trade.fee)}</TableCell>
                          <TableCell>{formatMoney(trade.slippage)}</TableCell>
                          <TableCell>{formatMoney(trade.netCashFlow)}</TableCell>
                          <TableCell>{formatTimestamp(trade.tradedAt)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                ) : (
                  <Typography sx={{ mt: 1.5 }} color="text.secondary">
                    체결 이력이 없습니다.
                  </Typography>
                )
              ) : null}

              {tabIndex === 2 ? (
                insight.audits.length > 0 ? (
                  <Table size="small" sx={{ mt: 1.5 }}>
                    <TableHead>
                      <TableRow>
                        <TableCell>Audit ID</TableCell>
                        <TableCell>액션</TableCell>
                        <TableCell>상태변경</TableCell>
                        <TableCell>사유</TableCell>
                        <TableCell>행위자</TableCell>
                        <TableCell>시각</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {insight.audits.map((audit) => (
                        <TableRow key={audit.auditId}>
                          <TableCell>{audit.auditId}</TableCell>
                          <TableCell>{audit.action}</TableCell>
                          <TableCell>{`${audit.fromStatus || "-"} -> ${audit.toStatus || "-"}`}</TableCell>
                          <TableCell>{audit.reason || "-"}</TableCell>
                          <TableCell>{audit.actor}</TableCell>
                          <TableCell>{formatTimestamp(audit.actedAt)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                ) : (
                  <Typography sx={{ mt: 1.5 }} color="text.secondary">
                    감사 이력이 없습니다.
                  </Typography>
                )
              ) : null}

              {tabIndex === 3 ? (
                insight.riskAlerts.length > 0 ? (
                  <Stack spacing={1.2} sx={{ mt: 1.5 }}>
                    {insight.riskAlerts.map((risk, idx) => (
                      <Box
                        key={`${risk.code}-${idx}`}
                        sx={{ border: 1, borderColor: "divider", borderRadius: 1.2, p: 1.2, backgroundColor: "background.paper" }}
                      >
                        <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 0.6 }}>
                          <Chip size="small" label={risk.severity} color={severityColor(risk.severity)} />
                          <Typography variant="subtitle2">{risk.code}</Typography>
                        </Stack>
                        <Typography variant="body2" sx={{ mb: 0.5 }}>
                          {risk.message}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {`${risk.metricName}: ${formatNumber(risk.metricValue)} / threshold: ${formatNumber(risk.thresholdValue)} · ${formatTimestamp(risk.occurredAt)}`}
                        </Typography>
                      </Box>
                    ))}
                  </Stack>
                ) : (
                  <Typography sx={{ mt: 1.5 }} color="text.secondary">
                    리스크 경보가 없습니다.
                  </Typography>
                )
              ) : null}
            </>
          ) : null}
        </DialogContent>
      </Dialog>
    </>
  );
}
