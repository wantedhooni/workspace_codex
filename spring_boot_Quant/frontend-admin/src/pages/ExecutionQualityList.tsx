import { Chip } from "@mui/material";
import {
  Datagrid,
  FunctionField,
  List,
  NumberField,
  TextField,
  TextInput
} from "react-admin";
import { PortfolioSelectInput } from "../components/PortfolioSelectInput";
import { RecordDetailButton } from "../components/RecordDetailButton";
import { usePortfolioCatalog } from "../portfolio/portfolioCatalog";
import { useSelectedPortfolioId } from "../portfolio/portfolioSelection";

function gradeColor(gradeRaw: unknown): "success" | "info" | "warning" | "error" | "default" {
  const grade = String(gradeRaw ?? "").toUpperCase();
  if (grade === "A") return "success";
  if (grade === "B") return "info";
  if (grade === "C") return "warning";
  if (grade === "D") return "error";
  return "default";
}

export function ExecutionQualityList() {
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const { formatPortfolioLabel } = usePortfolioCatalog();

  return (
    <List
      title="체결 품질"
      perPage={25}
      filters={[
        <PortfolioSelectInput key="portfolioId" source="portfolioId" label="포트폴리오" alwaysOn />,
        <TextInput key="symbol" source="symbol" label="심볼" alwaysOn />,
        <TextInput key="qualityGrade" source="qualityGrade" label="등급" />
      ]}
      filter={{ portfolioId: selectedPortfolioId }}
    >
      <Datagrid bulkActionButtons={false} rowClick={false}>
        <TextField source="qualityKey" label="Quality Key" />
        <FunctionField label="포트폴리오" render={(record) => formatPortfolioLabel(record.portfolioId)} />
        <TextField source="symbol" label="심볼" />
        <FunctionField
          label="등급"
          render={(record) => <Chip size="small" label={String(record.qualityGrade ?? "N/A")} color={gradeColor(record.qualityGrade)} />}
        />
        <NumberField source="orderCount" label="주문수" />
        <NumberField source="filledOrderCount" label="완전체결" />
        <NumberField source="fillRatePct" label="체결률(%)" />
        <NumberField source="tradeCount" label="체결건수" />
        <NumberField source="executedQuantity" label="체결수량" />
        <NumberField source="executedNotional" label="체결금액" />
        <NumberField source="averageFillPrice" label="평균체결가" />
        <NumberField source="averageFeeBps" label="평균수수료(bps)" />
        <NumberField source="averageSlippageBps" label="평균슬리피지(bps)" />
        <TextField source="qualityNote" label="진단" />
        <TextField source="lastTradedAt" label="마지막체결시각" />
        <FunctionField
          label="상세"
          render={() => (
            <RecordDetailButton
              title="체결 품질 상세"
              fields={[
                { source: "qualityKey", label: "Quality Key" },
                { source: "portfolioId", label: "Portfolio" },
                { source: "symbol", label: "심볼" },
                { source: "qualityGrade", label: "등급" },
                { source: "orderCount", label: "주문수" },
                { source: "filledOrderCount", label: "완전체결" },
                { source: "fillRatePct", label: "체결률(%)" },
                { source: "tradeCount", label: "체결건수" },
                { source: "executedQuantity", label: "체결수량" },
                { source: "executedNotional", label: "체결금액" },
                { source: "averageFillPrice", label: "평균체결가" },
                { source: "averageFeeBps", label: "평균수수료(bps)" },
                { source: "averageSlippageBps", label: "평균슬리피지(bps)" },
                { source: "netCashFlow", label: "순현금흐름" },
                { source: "qualityNote", label: "진단" },
                { source: "lastTradedAt", label: "마지막체결시각" }
              ]}
            />
          )}
        />
      </Datagrid>
    </List>
  );
}
