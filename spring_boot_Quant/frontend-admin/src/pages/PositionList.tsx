import { Datagrid, FunctionField, List, NumberField, TextField } from "react-admin";
import { PortfolioSelectInput } from "../components/PortfolioSelectInput";
import { RecordDetailButton } from "../components/RecordDetailButton";
import { usePortfolioCatalog } from "../portfolio/portfolioCatalog";
import { useSelectedPortfolioId } from "../portfolio/portfolioSelection";

const money = new Intl.NumberFormat("ko-KR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });

export function PositionList() {
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const { formatPortfolioLabel } = usePortfolioCatalog();

  return (
    <List
      title="포지션"
      perPage={25}
      filters={[<PortfolioSelectInput key="portfolioId" source="portfolioId" label="포트폴리오" alwaysOn />]}
      filter={{ portfolioId: selectedPortfolioId }}
    >
      <Datagrid bulkActionButtons={false} rowClick={false}>
        <TextField source="symbol" label="종목" />
        <NumberField source="quantity" label="보유수량" />
        <NumberField source="marketValue" label="평가금액" />
        <NumberField source="unrealizedPnl" label="미실현손익" />
        <NumberField source="realizedPnl" label="실현손익" />
        <FunctionField
          label="총손익"
          render={(record) => {
            const unreal = Number(record.unrealizedPnl ?? 0);
            const real = Number(record.realizedPnl ?? 0);
            return money.format(unreal + real);
          }}
        />
        <NumberField source="avgPrice" label="평균단가" />
        <NumberField source="lastPrice" label="현재가(최근체결)" />
        <FunctionField label="포트폴리오" render={(record) => formatPortfolioLabel(record.portfolioId)} />
        <FunctionField
          label="상세"
          render={() => (
            <RecordDetailButton
              title="포지션 상세"
              summaryItems={[
                {
                  label: "총 손익",
                  value: (record) => {
                    const unreal = Number(record.unrealizedPnl ?? 0);
                    const real = Number(record.realizedPnl ?? 0);
                    return money.format(unreal + real);
                  }
                },
                {
                  label: "미실현 수익률",
                  value: (record) => {
                    const mv = Math.abs(Number(record.marketValue ?? 0));
                    const unreal = Number(record.unrealizedPnl ?? 0);
                    if (mv <= 0) return "0.00%";
                    return `${((unreal / mv) * 100).toFixed(2)}%`;
                  }
                },
                {
                  label: "평균단가 대비 현재가",
                  value: (record) => {
                    const avg = Number(record.avgPrice ?? 0);
                    const last = Number(record.lastPrice ?? 0);
                    if (avg <= 0) return "0.00%";
                    return `${(((last - avg) / avg) * 100).toFixed(2)}%`;
                  }
                }
              ]}
              fields={[
                { source: "portfolioId", label: "Portfolio" },
                { source: "symbol", label: "종목" },
                { source: "quantity", label: "보유수량" },
                { source: "avgPrice", label: "평균단가" },
                { source: "lastPrice", label: "최근체결가" },
                { source: "marketValue", label: "평가금액" },
                { source: "unrealizedPnl", label: "미실현손익" },
                { source: "realizedPnl", label: "실현손익" }
              ]}
            />
          )}
        />
      </Datagrid>
    </List>
  );
}
