import { Chip } from "@mui/material";
import {
  Button,
  Datagrid,
  FunctionField,
  List,
  NumberField,
  NumberInput,
  SelectInput,
  TextField,
  TextInput,
  TopToolbar,
  useDataProvider,
  useListContext,
  minValue,
  useNotify,
  usePermissions,
  useRecordContext,
  useRefresh,
  required
} from "react-admin";
import { PortfolioSelectInput } from "../components/PortfolioSelectInput";
import { CreateDialogButton } from "../components/CreateDialogButton";
import { OrderWorkbenchPanel } from "../components/OrderWorkbenchPanel";
import { OrderInsightButton } from "../components/OrderInsightButton";
import type { AppPermissions } from "../providers/authProvider";
import { usePortfolioCatalog } from "../portfolio/portfolioCatalog";
import { useSelectedPortfolioId } from "../portfolio/portfolioSelection";

const usSymbolPattern = /^[A-Za-z][A-Za-z0-9.-]{0,9}$/;

const validateSymbol = (value: unknown) => {
  const text = String(value ?? "").trim();
  if (!text) {
    return "종목은 필수입니다.";
  }
  if (!usSymbolPattern.test(text)) {
    return "미국 종목 심볼 형식이 아닙니다. 예: AAPL";
  }
  return undefined;
};

const validateOrderForm = (values: Record<string, unknown>) => {
  const errors: Record<string, string> = {};
  const orderType = String(values.orderType ?? "MARKET");
  const limitPrice = Number(values.limitPrice ?? 0);

  if (orderType === "LIMIT" && (!Number.isFinite(limitPrice) || limitPrice <= 0)) {
    errors.limitPrice = "LIMIT 주문은 지정가(limitPrice)가 필수입니다.";
  }

  return Object.keys(errors).length > 0 ? errors : undefined;
};

const transformOrderForm = (values: Record<string, unknown>) => ({
  ...values,
  symbol: String(values.symbol ?? "").trim().toUpperCase()
});

function OrderListActions() {
  const { permissions } = usePermissions<AppPermissions>();
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const canCreate = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.orders?.canCreate);

  return (
    <TopToolbar>
      {canCreate ? (
        <CreateDialogButton
          resource="orders"
          title="주문 생성"
          defaultValues={{
            portfolioId: selectedPortfolioId,
            side: "BUY",
            orderType: "MARKET",
            timeInForce: "DAY",
            quantity: 1
          }}
          successMessage="주문이 생성되었습니다."
          formValidate={validateOrderForm}
          transform={transformOrderForm}
        >
          <PortfolioSelectInput source="portfolioId" label="포트폴리오" validate={[required()]} />
          <TextInput source="symbol" label="종목" validate={validateSymbol} />
          <SelectInput
            source="side"
            label="매수/매도"
            validate={required()}
            choices={[
              { id: "BUY", name: "BUY" },
              { id: "SELL", name: "SELL" }
            ]}
          />
          <SelectInput
            source="orderType"
            label="주문유형"
            validate={required()}
            choices={[
              { id: "MARKET", name: "MARKET" },
              { id: "LIMIT", name: "LIMIT" }
            ]}
          />
          <SelectInput
            source="timeInForce"
            label="TIF"
            validate={required()}
            choices={[
              { id: "DAY", name: "DAY" },
              { id: "GTC", name: "GTC" },
              { id: "IOC", name: "IOC" }
            ]}
          />
          <NumberInput source="limitPrice" label="지정가(LIMIT 필수)" validate={minValue(0.000001)} />
          <NumberInput source="quantity" label="수량" validate={[required(), minValue(1)]} />
        </CreateDialogButton>
      ) : null}
    </TopToolbar>
  );
}

function OrderBulkActionButtons() {
  const { selectedIds, onUnselectItems } = useListContext();
  const dp = useDataProvider();
  const notify = useNotify();
  const refresh = useRefresh();
  const { permissions } = usePermissions<AppPermissions>();
  const canUpdate = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.orders?.canUpdate);

  const executeBulk = async (action: "cancel" | "reject") => {
    if (!canUpdate) {
      notify("권한이 없습니다.", { type: "warning" });
      return;
    }
    if (!selectedIds || selectedIds.length === 0) {
      notify("선택된 주문이 없습니다.", { type: "warning" });
      return;
    }

    const reason = window.prompt(
      action === "cancel" ? "일괄 취소 사유를 입력하세요." : "일괄 거부 사유를 입력하세요.",
      action === "cancel" ? "bulk cancel from UI" : "bulk reject from UI"
    );
    if (!reason || !reason.trim()) {
      return;
    }

    try {
      const res = await dp.create("orderBulkActions", {
        data: {
          action,
          orderIds: selectedIds,
          reason: reason.trim()
        }
      });

      const successCount = Number((res.data as Record<string, unknown>)?.successCount ?? 0);
      const failedCount = Number((res.data as Record<string, unknown>)?.failedCount ?? 0);

      notify(
        `${action === "cancel" ? "일괄 취소" : "일괄 거부"} 완료: 성공 ${successCount}건 / 실패 ${failedCount}건`,
        { type: failedCount > 0 ? "warning" : "info" }
      );
      onUnselectItems?.();
      refresh();
    } catch (error) {
      notify(error instanceof Error ? error.message : "일괄 처리 실패", { type: "warning" });
    }
  };

  if (!canUpdate) {
    return null;
  }

  return (
    <>
      <Button label="선택 취소" onClick={() => void executeBulk("cancel")} />
      <Button label="선택 거부" onClick={() => void executeBulk("reject")} />
    </>
  );
}

function OrderActionButtons() {
  const record = useRecordContext();
  const dp = useDataProvider();
  const notify = useNotify();
  const refresh = useRefresh();
  const { permissions } = usePermissions<AppPermissions>();

  if (!record) return null;

  const canDelete = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.orders?.canDelete);
  const canUpdate = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.orders?.canUpdate);
  const filledQuantity = Number(record.filledQuantity ?? 0);
  const status = String(record.status ?? "");
  const canCancelByStatus = status === "NEW" || status === "SENT" || status === "PARTIAL";
  const canRejectByStatus = (status === "NEW" || status === "SENT") && filledQuantity <= 0;

  return (
    <>
      <Button
        label="취소"
        disabled={!canUpdate || !canCancelByStatus}
        onClick={async () => {
          try {
            await dp.update("orders", {
              id: record.orderId,
              data: { reason: "manual cancel from UI" },
              previousData: record,
              meta: { action: "cancel" }
            });
            notify("주문 취소 완료", { type: "info" });
            refresh();
          } catch (error) {
            notify(error instanceof Error ? error.message : "주문 취소 실패", { type: "warning" });
          }
        }}
      />
      <Button
        label="거부"
        disabled={!canUpdate || !canRejectByStatus}
        onClick={async () => {
          try {
            await dp.update("orders", {
              id: record.orderId,
              data: { reason: "risk reject from UI" },
              previousData: record,
              meta: { action: "reject" }
            });
            notify("주문 거부 완료", { type: "info" });
            refresh();
          } catch (error) {
            notify(error instanceof Error ? error.message : "주문 거부 실패", { type: "warning" });
          }
        }}
      />
      <Button
        label="삭제"
        disabled={!canDelete || filledQuantity > 0}
        onClick={async () => {
          try {
            await dp.delete("orders", { id: record.orderId, previousData: record });
            notify("주문 삭제 완료", { type: "info" });
            refresh();
          } catch (error) {
            notify(error instanceof Error ? error.message : "주문 삭제 실패", { type: "warning" });
          }
        }}
      />
    </>
  );
}

export function OrderList() {
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const { formatPortfolioLabel } = usePortfolioCatalog();

  const statusChipColor = (status: string) => {
    if (status === "FILLED") return "success" as const;
    if (status === "PARTIAL") return "warning" as const;
    if (status === "NEW" || status === "SENT") return "info" as const;
    if (status === "REJECTED" || status === "CANCELED") return "default" as const;
    return "default" as const;
  };

  return (
    <List
      title="주문 목록"
      perPage={25}
      actions={<OrderListActions />}
      filters={[
        <PortfolioSelectInput key="portfolioId" source="portfolioId" label="포트폴리오" alwaysOn />,
        <SelectInput
          key="status"
          source="status"
          label="상태"
          emptyText="전체"
          choices={[
            { id: "NEW", name: "NEW" },
            { id: "SENT", name: "SENT" },
            { id: "PARTIAL", name: "PARTIAL" },
            { id: "FILLED", name: "FILLED" },
            { id: "CANCELED", name: "CANCELED" },
            { id: "REJECTED", name: "REJECTED" }
          ]}
        />,
        <SelectInput
          key="side"
          source="side"
          label="매수/매도"
          emptyText="전체"
          choices={[
            { id: "BUY", name: "BUY" },
            { id: "SELL", name: "SELL" }
          ]}
        />,
        <TextInput key="symbol" source="symbol" label="종목" />,
        <SelectInput
          key="orderType"
          source="orderType"
          label="주문유형"
          emptyText="전체"
          choices={[
            { id: "MARKET", name: "MARKET" },
            { id: "LIMIT", name: "LIMIT" }
          ]}
        />,
        <SelectInput
          key="timeInForce"
          source="timeInForce"
          label="TIF"
          emptyText="전체"
          choices={[
            { id: "DAY", name: "DAY" },
            { id: "GTC", name: "GTC" },
            { id: "IOC", name: "IOC" }
          ]}
        />,
        <NumberInput key="minRemainingQuantity" source="minRemainingQuantity" label="잔여수량 >= " min={0} />,
        <NumberInput key="maxRemainingQuantity" source="maxRemainingQuantity" label="잔여수량 <= " min={0} />,
        <NumberInput key="minQuantity" source="minQuantity" label="수량 >= " min={0} />,
        <NumberInput key="maxQuantity" source="maxQuantity" label="수량 <= " min={0} />
      ]}
      filter={{ portfolioId: selectedPortfolioId }}
    >
      <OrderWorkbenchPanel />
      <Datagrid bulkActionButtons={<OrderBulkActionButtons />} rowClick={false}>
        <FunctionField
          label="상태"
          render={(record) => {
            const status = String(record.status ?? "-");
            return <Chip size="small" label={status} color={statusChipColor(status)} variant="outlined" />;
          }}
        />
        <TextField source="symbol" label="종목" />
        <TextField source="side" label="매수/매도" />
        <NumberField source="remainingQuantity" label="잔여수량" />
        <NumberField source="fillRate" label="체결률(%)" />
        <NumberField source="quantity" label="주문수량" />
        <NumberField source="filledQuantity" label="체결수량" />
        <TextField source="orderType" label="주문유형" />
        <TextField source="timeInForce" label="TIF" />
        <TextField source="createdAt" label="생성시각" />
        <FunctionField label="포트폴리오" render={(record) => formatPortfolioLabel(record.portfolioId)} />
        <NumberField source="orderId" label="Order ID" />
        <FunctionField label="상세" render={() => <OrderInsightButton />} />
        <FunctionField label="액션" render={() => <OrderActionButtons />} />
      </Datagrid>
    </List>
  );
}
