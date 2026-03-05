import { Button, Card, Table, Tag } from "antd";
import type { StockOrder } from "../api";

type StockOrdersPageProps = {
  stockOrders: StockOrder[];
  completingOrderId: string | null;
  onCompleteFill: (orderId: string) => void;
};

export function StockOrdersPage({ stockOrders, completingOrderId, onCompleteFill }: StockOrdersPageProps) {
  return (
    <Card title="Stock Orders">
      <Table
        rowKey="id"
        dataSource={stockOrders}
        pagination={false}
        columns={[
          { title: "Order No", dataIndex: "orderNumber" },
          { title: "Symbol", dataIndex: "symbol" },
          { title: "Market", dataIndex: "market" },
          { title: "Side", dataIndex: "side" },
          { title: "TIF", dataIndex: "timeInForce" },
          { title: "Quantity", render: (_, record: StockOrder) => Number(record.quantity).toLocaleString() },
          { title: "Limit Price", render: (_, record: StockOrder) => `${Number(record.limitPrice).toLocaleString()} ${record.currency}` },
          { title: "Gross", render: (_, record: StockOrder) => `${Number(record.grossAmount).toLocaleString()} ${record.currency}` },
          {
            title: "Charges",
            render: (_, record: StockOrder) => (
              <div>
                <div>{`fee ${Number(record.feeAmount).toLocaleString()} ${record.currency}`}</div>
                <small>{`tax ${Number(record.taxAmount).toLocaleString()} / net ${Number(record.netSettlementAmount).toLocaleString()}`}</small>
              </div>
            ),
          },
          {
            title: "Status",
            dataIndex: "status",
            render: (value: string) => (
              <Tag color={value === "APPROVED" ? "green" : value === "REJECTED" ? "red" : value === "CANCELED" ? "default" : value === "PARTIALLY_FILLED" ? "blue" : "gold"}>
                {value}
              </Tag>
            ),
          },
          {
            title: "Memo",
            render: (_, record: StockOrder) => record.orderMemo ?? "-",
          },
          {
            title: "Policy",
            render: (_, record: StockOrder) => (
              <div>
                <div>{record.status === "CANCELED" ? "Canceled by user" : record.manualReviewRequired ? "Manual review" : "Standard queue"}</div>
                <small>
                  {record.status === "CANCELED"
                    ? record.cancellationReason ?? "-"
                    : record.manualReviewRequired ? record.manualReviewReason ?? "-" : "No additional review"}
                </small>
              </div>
            ),
          },
          {
            title: "Execution Window",
            render: (_, record: StockOrder) => (
              <div>
                <div>{record.marketSession}</div>
                <small>{new Date(record.expectedExecutionAt).toLocaleString()}</small>
              </div>
            ),
          },
          {
            title: "Expires At",
            render: (_, record: StockOrder) => new Date(record.expiresAt).toLocaleString(),
          },
          {
            title: "Canceled At",
            render: (_, record: StockOrder) => record.canceledAt ? new Date(record.canceledAt).toLocaleString() : "-",
          },
          {
            title: "Fill Progress",
            render: (_, record: StockOrder) => record.executions.length
              ? (
                <div>
                  <div>{`${Number(record.executedQuantity ?? 0).toLocaleString()} / ${Number(record.quantity).toLocaleString()}`}</div>
                  <small>{`${Math.round(Number(record.fillRate ?? 0) * 100)}% filled / remaining ${Number(record.remainingQuantity ?? 0).toLocaleString()}`}</small>
                </div>
              )
              : `${Number(record.remainingQuantity ?? record.quantity).toLocaleString()} open`,
          },
          {
            title: "Execution History",
            render: (_, record: StockOrder) => record.executions.length
              ? (
                <div>
                  {record.executions.map((execution) => (
                    <div key={execution.id}>
                      <small>{`${execution.executionNumber} / ${Number(execution.executedQuantity).toLocaleString()} @ ${Number(execution.executedPrice).toLocaleString()}`}</small>
                    </div>
                  ))}
                </div>
              )
              : "-",
          },
          {
            title: "Action",
            render: (_, record: StockOrder) => record.status === "PARTIALLY_FILLED"
              ? (
                <Button
                  size="small"
                  type="primary"
                  loading={completingOrderId === record.id}
                  onClick={() => onCompleteFill(record.id)}
                >
                  Complete Fill
                </Button>
              )
              : "-",
          },
          { title: "Settlement Txn", dataIndex: "settlementTransactionNumber" },
          { title: "Settled At", render: (_, record: StockOrder) => record.settledAt ? new Date(record.settledAt).toLocaleString() : "-" },
          { title: "Created At", render: (_, record: StockOrder) => new Date(record.createdAt).toLocaleString() },
        ]}
      />
    </Card>
  );
}
