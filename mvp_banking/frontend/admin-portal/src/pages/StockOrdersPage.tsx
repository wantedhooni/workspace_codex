import { Button, Card, Col, Row, Tag, Typography } from "antd";
import type { StockOrder } from "../api";
import { OperationsGridTable } from "../components/OperationsGridTable";

type StockOrdersPageProps = {
  stockOrders: StockOrder[];
  completingOrderId: string | null;
  onCompleteFill: (orderId: string) => void;
};

export function StockOrdersPage({ stockOrders, completingOrderId, onCompleteFill }: StockOrdersPageProps) {
  const pendingCount = stockOrders.filter((item) => item.status === "PENDING_APPROVAL").length;
  const partiallyFilledCount = stockOrders.filter((item) => item.status === "PARTIALLY_FILLED").length;
  const manualReviewCount = stockOrders.filter((item) => item.manualReviewRequired).length;
  const pendingNotional = stockOrders
    .filter((item) => item.status === "PENDING_APPROVAL" || item.status === "PARTIALLY_FILLED")
    .reduce((sum, item) => sum + Number(item.grossAmount), 0);

  return (
    <>
      <Card className="ops-hero-card" bordered={false}>
        <div className="ops-page-header">
          <div>
            <p className="eyebrow">Trading Desk</p>
            <Typography.Title level={2} style={{ marginBottom: 8 }}>
              Stock Orders
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ margin: 0, maxWidth: 760 }}>
              주문 심사, 부분 체결 후속 처리, 정산 상태 확인을 한 화면에 모은 운영형 주식 주문 대기함입니다. 실행 윈도우와 정책 플래그를 표에서 바로 읽을 수 있도록 구성했습니다.
            </Typography.Paragraph>
          </div>
          <div className="ops-header-meta">
            <span>Open Order Load</span>
            <strong>{pendingCount + partiallyFilledCount}건</strong>
            <small>{`대기 명목금액 ${pendingNotional.toLocaleString()}`}</small>
          </div>
        </div>
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Card className="ops-stat-card">
            <Typography.Text type="secondary">Pending Approval</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0", color: pendingCount > 0 ? "#d46b08" : undefined }}>
              {pendingCount}건
            </Typography.Title>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="ops-stat-card">
            <Typography.Text type="secondary">Partial Fill</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0", color: partiallyFilledCount > 0 ? "#0f6ab4" : undefined }}>
              {partiallyFilledCount}건
            </Typography.Title>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="ops-stat-card">
            <Typography.Text type="secondary">Manual Review</Typography.Text>
            <Typography.Title level={3} style={{ margin: "12px 0 0", color: manualReviewCount > 0 ? "#cf1322" : undefined }}>
              {manualReviewCount}건
            </Typography.Title>
          </Card>
        </Col>
      </Row>

      <Card className="ops-panel-card" title="Stock Orders">
      <OperationsGridTable
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
    </>
  );
}
