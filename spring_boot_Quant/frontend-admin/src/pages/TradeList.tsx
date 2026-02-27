import {
  Datagrid,
  FunctionField,
  List,
  NumberField,
  NumberInput,
  TextField,
  TextInput,
  TopToolbar,
  minValue,
  required,
  usePermissions
} from "react-admin";
import { PortfolioSelectInput } from "../components/PortfolioSelectInput";
import { CreateDialogButton } from "../components/CreateDialogButton";
import { RecordDetailButton } from "../components/RecordDetailButton";
import type { AppPermissions } from "../providers/authProvider";
import { usePortfolioCatalog } from "../portfolio/portfolioCatalog";
import { useSelectedPortfolioId } from "../portfolio/portfolioSelection";

const money = new Intl.NumberFormat("ko-KR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });

const validateTradeForm = (values: Record<string, unknown>) => {
  const errors: Record<string, string> = {};
  const qty = Number(values.tradeQuantity ?? 0);
  const px = Number(values.tradePrice ?? 0);
  const orderId = Number(values.orderId ?? 0);

  if (!Number.isFinite(orderId) || orderId <= 0) {
    errors.orderId = "유효한 Order ID가 필요합니다.";
  }
  if (!Number.isFinite(qty) || qty <= 0) {
    errors.tradeQuantity = "체결수량은 0보다 커야 합니다.";
  }
  if (!Number.isFinite(px) || px <= 0) {
    errors.tradePrice = "체결가는 0보다 커야 합니다.";
  }

  return Object.keys(errors).length > 0 ? errors : undefined;
};

function TradeListActions() {
  const { permissions } = usePermissions<AppPermissions>();
  const canCreate = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.trades?.canCreate);

  return (
    <TopToolbar>
      {canCreate ? (
        <CreateDialogButton
          resource="trades"
          title="체결 이벤트 등록"
          defaultValues={{ tradeQuantity: 1, tradePrice: 100 }}
          successMessage="체결 이벤트가 등록되었습니다."
          maxWidth="sm"
          formValidate={validateTradeForm}
        >
          <NumberInput source="orderId" label="Order ID" validate={[required(), minValue(1)]} />
          <NumberInput source="tradeQuantity" label="체결수량" validate={[required(), minValue(0.000001)]} />
          <NumberInput source="tradePrice" label="체결가" validate={[required(), minValue(0.000001)]} />
        </CreateDialogButton>
      ) : null}
    </TopToolbar>
  );
}

export function TradeList() {
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const { formatPortfolioLabel } = usePortfolioCatalog();

  return (
    <List
      title="체결 목록"
      perPage={25}
      actions={<TradeListActions />}
      filters={[
        <PortfolioSelectInput key="portfolioId" source="portfolioId" label="포트폴리오" alwaysOn />,
        <TextInput key="symbol" source="symbol" label="종목" />,
        <NumberInput key="orderId" source="orderId" label="Order ID" />
      ]}
      filter={{ portfolioId: selectedPortfolioId }}
    >
      <Datagrid bulkActionButtons={false} rowClick={false}>
        <TextField source="symbol" label="종목" />
        <TextField source="side" label="매수/매도" />
        <NumberField source="tradeQuantity" label="수량" />
        <NumberField source="tradePrice" label="체결가" />
        <NumberField source="notional" label="체결금액" />
        <NumberField source="fee" label="수수료" />
        <NumberField source="slippage" label="슬리피지" />
        <NumberField source="netCashFlow" label="순현금흐름" />
        <TextField source="tradedAt" label="체결시각" />
        <FunctionField label="포트폴리오" render={(record) => formatPortfolioLabel(record.portfolioId)} />
        <NumberField source="orderId" label="Order ID" />
        <NumberField source="tradeId" label="Trade ID" />
        <FunctionField
          label="상세"
          render={() => (
            <RecordDetailButton
              title="체결 상세"
              summaryItems={[
                {
                  label: "총 거래원가",
                  value: (record) => {
                    const notional = Number(record.notional ?? 0);
                    const fee = Number(record.fee ?? 0);
                    const slippage = Number(record.slippage ?? 0);
                    return money.format(notional + fee + slippage);
                  }
                },
                {
                  label: "수수료율(bps)",
                  value: (record) => {
                    const notional = Number(record.notional ?? 0);
                    const fee = Number(record.fee ?? 0);
                    if (notional <= 0) return "0.00";
                    return ((fee / notional) * 10000).toFixed(2);
                  }
                },
                {
                  label: "슬리피지율(bps)",
                  value: (record) => {
                    const notional = Number(record.notional ?? 0);
                    const slippage = Number(record.slippage ?? 0);
                    if (notional <= 0) return "0.00";
                    return ((slippage / notional) * 10000).toFixed(2);
                  }
                }
              ]}
              fields={[
                { source: "tradeId", label: "Trade ID" },
                { source: "portfolioId", label: "Portfolio" },
                { source: "orderId", label: "Order ID" },
                { source: "symbol", label: "종목" },
                { source: "side", label: "매수/매도" },
                { source: "tradeQuantity", label: "체결수량" },
                { source: "tradePrice", label: "체결가" },
                { source: "notional", label: "체결금액" },
                { source: "fee", label: "수수료" },
                { source: "slippage", label: "슬리피지" },
                { source: "netCashFlow", label: "순현금흐름" },
                { source: "tradedAt", label: "체결시각" }
              ]}
            />
          )}
        />
      </Datagrid>
    </List>
  );
}
