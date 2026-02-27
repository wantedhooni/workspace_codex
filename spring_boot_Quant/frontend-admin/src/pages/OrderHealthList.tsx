import { Chip } from "@mui/material";
import {
  Button,
  Datagrid,
  FunctionField,
  List,
  NumberField,
  NumberInput,
  TextField,
  TextInput,
  useDataProvider,
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

function healthColor(statusRaw: unknown): "success" | "warning" | "error" | "default" {
  const status = String(statusRaw ?? "").toUpperCase();
  if (status === "CRITICAL") return "error";
  if (status === "WARN") return "warning";
  if (status === "HEALTHY") return "success";
  return "default";
}

function OrderHealthActionButton() {
  const record = useRecordContext();
  const dp = useDataProvider();
  const notify = useNotify();
  const refresh = useRefresh();
  const { permissions } = usePermissions<AppPermissions>();

  if (!record) return null;

  const canUpdate = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.orderHealth?.canUpdate);

  return (
    <Button
      label="지연주문 일괄취소"
      disabled={!canUpdate}
      onClick={async () => {
        try {
          const result = await dp.update("orderHealth", {
            id: String(record.healthKey ?? `${record.portfolioId}_${record.symbol}`),
            data: {
              portfolioId: record.portfolioId,
              symbol: record.symbol,
              staleMinutes: record.staleThresholdMinutes,
              reason: "manual stale remediation from ui"
            },
            previousData: record,
            meta: { action: "remediateStale" }
          });

          const canceled = Number((result.data as Record<string, unknown>)?.canceledCount ?? 0);
          if (canceled > 0) {
            notify(`지연주문 ${canceled}건 취소 완료`, { type: "info" });
          } else {
            notify("취소 대상 지연주문이 없습니다.", { type: "info" });
          }
          refresh();
        } catch (error) {
          notify(error instanceof Error ? error.message : "지연주문 일괄취소 실패", { type: "warning" });
        }
      }}
    />
  );
}

export function OrderHealthList() {
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const { formatPortfolioLabel } = usePortfolioCatalog();

  return (
    <List
      title="주문 건전성"
      perPage={25}
      filters={[
        <PortfolioSelectInput key="portfolioId" source="portfolioId" label="포트폴리오" alwaysOn />,
        <TextInput key="symbol" source="symbol" label="심볼" />,
        <NumberInput key="staleMinutes" source="staleMinutes" label="지연 기준(분)" />,
        <TextInput key="healthStatus" source="healthStatus" label="상태" />
      ]}
      filter={{ portfolioId: selectedPortfolioId }}
    >
      <Datagrid bulkActionButtons={false} rowClick={false}>
        <TextField source="healthKey" label="Health Key" />
        <FunctionField label="포트폴리오" render={(record) => formatPortfolioLabel(record.portfolioId)} />
        <TextField source="symbol" label="심볼" />
        <FunctionField
          label="상태"
          render={(record) => <Chip size="small" label={String(record.healthStatus ?? "N/A")} color={healthColor(record.healthStatus)} />}
        />
        <NumberField source="openOrderCount" label="오픈주문수" />
        <NumberField source="staleOrderCount" label="지연주문수" />
        <NumberField source="staleThresholdMinutes" label="지연기준(분)" />
        <NumberField source="openOrderUsagePct" label="한도사용률(%)" />
        <NumberField source="averageOpenAgeMinutes" label="평균오픈경과(분)" />
        <NumberField source="maxOpenAgeMinutes" label="최대오픈경과(분)" />
        <NumberField source="oldestOpenOrderId" label="최장기주문ID" />
        <TextField source="healthNote" label="진단" />
        <TextField source="updatedAt" label="계산시각" />
        <FunctionField
          label="상세"
          render={() => (
            <RecordDetailButton
              title="주문 건전성 상세"
              fields={[
                { source: "healthKey", label: "Health Key" },
                { source: "portfolioId", label: "Portfolio" },
                { source: "symbol", label: "심볼" },
                { source: "healthStatus", label: "상태" },
                { source: "openOrderCount", label: "오픈주문수" },
                { source: "staleOrderCount", label: "지연주문수" },
                { source: "staleThresholdMinutes", label: "지연기준(분)" },
                { source: "openOrderUsagePct", label: "한도사용률(%)" },
                { source: "averageOpenAgeMinutes", label: "평균오픈경과(분)" },
                { source: "maxOpenAgeMinutes", label: "최대오픈경과(분)" },
                { source: "oldestOpenOrderId", label: "최장기주문ID" },
                { source: "healthNote", label: "진단" },
                { source: "updatedAt", label: "계산시각" }
              ]}
            />
          )}
        />
        <FunctionField label="액션" render={() => <OrderHealthActionButton />} />
      </Datagrid>
    </List>
  );
}
