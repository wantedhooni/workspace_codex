import {
  Chip,
  Dialog,
  DialogContent,
  DialogTitle,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography
} from "@mui/material";
import { useState } from "react";
import {
  Button,
  Datagrid,
  FunctionField,
  List,
  NumberField,
  NumberInput,
  TextField,
  TopToolbar,
  minValue,
  required,
  useDataProvider,
  useNotify,
  usePermissions,
  useRecordContext,
  useRefresh
} from "react-admin";
import { PortfolioSelectInput } from "../components/PortfolioSelectInput";
import { CreateDialogButton } from "../components/CreateDialogButton";
import { RecordDetailButton } from "../components/RecordDetailButton";
import type { AppPermissions } from "../providers/authProvider";
import { usePortfolioCatalog } from "../portfolio/portfolioCatalog";
import { useSelectedPortfolioId } from "../portfolio/portfolioSelection";

const money = new Intl.NumberFormat("ko-KR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });

type TradingControlHistoryRow = {
  id: number | string;
  historyId?: number;
  portfolioId?: number;
  previousTradingEnabled?: boolean;
  tradingEnabled?: boolean;
  action?: string;
  reason?: string;
  updatedAt?: string;
  updatedBy?: string;
};

const validateRiskLimitForm = (values: Record<string, unknown>) => {
  const errors: Record<string, string> = {};
  const maxOrderNotional = Number(values.maxOrderNotional ?? 0);
  const maxPositionNotionalPerSymbol = Number(values.maxPositionNotionalPerSymbol ?? 0);
  const maxDailyTurnover = Number(values.maxDailyTurnover ?? 0);

  if (maxPositionNotionalPerSymbol < maxOrderNotional) {
    errors.maxPositionNotionalPerSymbol = "종목별 한도는 주문금액 한도보다 크거나 같아야 합니다.";
  }
  if (maxDailyTurnover < maxOrderNotional) {
    errors.maxDailyTurnover = "일일 회전율 한도는 주문금액 한도보다 크거나 같아야 합니다.";
  }

  return Object.keys(errors).length > 0 ? errors : undefined;
};

function RiskLimitListActions() {
  const { permissions } = usePermissions<AppPermissions>();
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const canCreate = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.riskLimits?.canCreate);

  return (
    <TopToolbar>
      {canCreate ? (
        <CreateDialogButton
          resource="riskLimits"
          title="리스크 한도 설정"
          defaultValues={{
            portfolioId: selectedPortfolioId,
            maxOrderNotional: 1000000,
            maxPositionNotionalPerSymbol: 3000000,
            maxDailyTurnover: 5000000,
            maxOpenOrdersPerSymbol: 20,
            commissionBps: 2.5,
            slippageBps: 1.5
          }}
          successMessage="리스크 한도가 저장되었습니다."
          formValidate={validateRiskLimitForm}
        >
          <PortfolioSelectInput source="portfolioId" label="포트폴리오" validate={[required()]} />
          <NumberInput source="maxOrderNotional" label="주문금액 한도" validate={[required(), minValue(0.01)]} />
          <NumberInput
            source="maxPositionNotionalPerSymbol"
            label="종목별 포지션 한도"
            validate={[required(), minValue(0.01)]}
          />
          <NumberInput source="maxDailyTurnover" label="일일 회전율 한도" validate={[required(), minValue(0.01)]} />
          <NumberInput source="maxOpenOrdersPerSymbol" label="종목별 오픈주문 수" validate={[required(), minValue(1)]} />
          <NumberInput source="commissionBps" label="수수료(bps)" validate={[required(), minValue(0)]} />
          <NumberInput source="slippageBps" label="슬리피지(bps)" validate={[required(), minValue(0)]} />
        </CreateDialogButton>
      ) : null}
    </TopToolbar>
  );
}

function TradingControlButtons() {
  const record = useRecordContext<Record<string, unknown>>();
  const dp = useDataProvider();
  const notify = useNotify();
  const refresh = useRefresh();
  const { permissions } = usePermissions<AppPermissions>();

  if (!record) return null;

  const canUpdate = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.riskLimits?.canUpdate);
  const enabled = Boolean(record.tradingEnabled ?? true);

  const toggle = async (tradingEnabled: boolean, reason: string) => {
    try {
      await dp.update("riskLimits", {
        id: record.portfolioId,
        data: {
          portfolioId: record.portfolioId,
          tradingEnabled,
          reason
        },
        previousData: record,
        meta: { action: "toggleTrading" }
      });
      notify(tradingEnabled ? "거래 재개 처리 완료" : "거래 중지 처리 완료", { type: "info" });
      refresh();
    } catch (error) {
      notify(error instanceof Error ? error.message : "거래 통제 변경 실패", { type: "warning" });
    }
  };

  return enabled ? (
    <Button label="거래중지" disabled={!canUpdate} onClick={() => toggle(false, "manual kill switch from ui")} />
  ) : (
    <Button label="거래재개" disabled={!canUpdate} onClick={() => toggle(true, "manual resume from ui")} />
  );
}

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

function TradingControlHistoryButton() {
  const record = useRecordContext<Record<string, unknown>>();
  const dp = useDataProvider();
  const notify = useNotify();
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState<TradingControlHistoryRow[]>([]);

  if (!record) {
    return null;
  }

  const loadHistory = async () => {
    const portfolioId = Number(record.portfolioId ?? 0);
    if (!Number.isFinite(portfolioId) || portfolioId <= 0) {
      notify("포트폴리오 ID를 확인할 수 없습니다.", { type: "warning" });
      return;
    }

    setLoading(true);
    try {
      const response = await dp.getList("tradingControlHistory", {
        filter: { portfolioId, limit: 50 },
        pagination: { page: 1, perPage: 50 },
        sort: { field: "updatedAt", order: "DESC" }
      });
      setRows((response.data ?? []) as TradingControlHistoryRow[]);
      setOpen(true);
    } catch (error) {
      notify(error instanceof Error ? error.message : "거래 통제 이력 조회 실패", { type: "warning" });
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Button label="이력" onClick={loadHistory} />
      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="lg">
        <DialogTitle>거래 통제 이력</DialogTitle>
        <DialogContent>
          {loading ? <Typography>이력을 불러오는 중입니다.</Typography> : null}
          {!loading && rows.length === 0 ? <Typography>거래 통제 이력이 없습니다.</Typography> : null}
          {!loading && rows.length > 0 ? (
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>이력 ID</TableCell>
                  <TableCell>포트폴리오</TableCell>
                  <TableCell>이전상태</TableCell>
                  <TableCell>변경상태</TableCell>
                  <TableCell>액션</TableCell>
                  <TableCell>사유</TableCell>
                  <TableCell>행위자</TableCell>
                  <TableCell>수행시각</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {rows.map((row) => (
                  <TableRow key={row.id}>
                    <TableCell>{row.historyId ?? "-"}</TableCell>
                    <TableCell>{row.portfolioId ?? "-"}</TableCell>
                    <TableCell>
                      {row.previousTradingEnabled === undefined ? "-" : row.previousTradingEnabled ? "ENABLED" : "DISABLED"}
                    </TableCell>
                    <TableCell>{row.tradingEnabled ? "ENABLED" : "DISABLED"}</TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={row.action ?? "-"}
                        color={row.action === "DISABLE" ? "error" : row.action === "ENABLE" ? "success" : "default"}
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell>{row.reason || "-"}</TableCell>
                    <TableCell>{row.updatedBy || "-"}</TableCell>
                    <TableCell>{formatDateTime(row.updatedAt)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          ) : null}
        </DialogContent>
      </Dialog>
    </>
  );
}

function TradingControlActionButtons() {
  return (
    <Stack direction="row" spacing={1}>
      <TradingControlButtons />
      <TradingControlHistoryButton />
    </Stack>
  );
}

export function RiskLimitList() {
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const { formatPortfolioLabel } = usePortfolioCatalog();

  return (
    <List
      title="리스크 한도"
      perPage={25}
      actions={<RiskLimitListActions />}
      filters={[<PortfolioSelectInput key="portfolioId" source="portfolioId" label="포트폴리오" alwaysOn />]}
      filter={{ portfolioId: selectedPortfolioId }}
    >
      <Datagrid bulkActionButtons={false} rowClick={false}>
        <FunctionField label="포트폴리오" render={(record) => formatPortfolioLabel(record.portfolioId)} />
        <NumberField source="maxOrderNotional" label="주문금액 한도" />
        <NumberField source="maxPositionNotionalPerSymbol" label="종목별 포지션 한도" />
        <NumberField source="maxDailyTurnover" label="일일 회전율 한도" />
        <NumberField source="maxOpenOrdersPerSymbol" label="종목별 오픈주문 수" />
        <NumberField source="commissionBps" label="수수료(bps)" />
        <NumberField source="slippageBps" label="슬리피지(bps)" />
        <FunctionField
          label="거래상태"
          render={(record) => (Boolean(record.tradingEnabled ?? true) ? "ENABLED" : "DISABLED")}
        />
        <TextField source="killSwitchReason" label="중지사유" />
        <TextField source="killSwitchUpdatedBy" label="변경자" />
        <TextField source="killSwitchUpdatedAt" label="변경시각" />
        <FunctionField
          label="상세"
          render={() => (
            <RecordDetailButton
              title="리스크 한도 상세"
              summaryItems={[
                {
                  label: "주문 대비 종목한도 배수",
                  value: (record) => {
                    const maxOrder = Number(record.maxOrderNotional ?? 0);
                    const maxPosition = Number(record.maxPositionNotionalPerSymbol ?? 0);
                    if (maxOrder <= 0) return "0.00x";
                    return `${(maxPosition / maxOrder).toFixed(2)}x`;
                  }
                },
                {
                  label: "일회전/주문 한도 배수",
                  value: (record) => {
                    const maxOrder = Number(record.maxOrderNotional ?? 0);
                    const maxTurnover = Number(record.maxDailyTurnover ?? 0);
                    if (maxOrder <= 0) return "0.00x";
                    return `${(maxTurnover / maxOrder).toFixed(2)}x`;
                  }
                },
                {
                  label: "주문금액 한도",
                  value: (record) => money.format(Number(record.maxOrderNotional ?? 0))
                },
                {
                  label: "거래상태",
                  value: (record) => (Boolean(record.tradingEnabled ?? true) ? "ENABLED" : "DISABLED")
                }
              ]}
              fields={[
                { source: "portfolioId", label: "Portfolio" },
                { source: "maxOrderNotional", label: "주문금액 한도" },
                { source: "maxPositionNotionalPerSymbol", label: "종목별 포지션 한도" },
                { source: "maxDailyTurnover", label: "일일 회전율 한도" },
                { source: "maxOpenOrdersPerSymbol", label: "종목별 오픈주문 수" },
                { source: "commissionBps", label: "수수료(bps)" },
                { source: "slippageBps", label: "슬리피지(bps)" },
                { source: "tradingEnabled", label: "거래상태" },
                { source: "killSwitchReason", label: "중지사유" },
                { source: "killSwitchUpdatedBy", label: "변경자" },
                { source: "killSwitchUpdatedAt", label: "변경시각" }
              ]}
            />
          )}
        />
        <FunctionField label="액션" render={() => <TradingControlActionButtons />} />
      </Datagrid>
    </List>
  );
}
