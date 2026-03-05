import { Card, Tag } from "antd";
import type { StockPosition } from "../api";
import { OperationsGridTable } from "../components/OperationsGridTable";

type StockPositionsPageProps = {
  stockPositions: StockPosition[];
};

export function StockPositionsPage({ stockPositions }: StockPositionsPageProps) {
  return (
    <Card title="Stock Positions">
      <OperationsGridTable
        rowKey="id"
        dataSource={stockPositions}
        pagination={false}
        columns={[
          { title: "Symbol", dataIndex: "symbol" },
          { title: "Market", dataIndex: "market" },
          { title: "Quantity", render: (_, record: StockPosition) => Number(record.quantity).toLocaleString() },
          { title: "Average Price", render: (_, record: StockPosition) => `${Number(record.averagePrice).toLocaleString()} ${record.currency}` },
          {
            title: "Current Price",
            render: (_, record: StockPosition) => record.currentPrice !== null ? `${Number(record.currentPrice).toLocaleString()} ${record.currency}` : "-",
          },
          {
            title: "Market Value",
            render: (_, record: StockPosition) => `${Number(record.marketValue ?? record.costBasis).toLocaleString()} ${record.currency}`,
          },
          {
            title: "Unrealized P/L",
            render: (_, record: StockPosition) => (
              <Tag color={Number(record.unrealizedProfitLoss ?? 0) >= 0 ? "blue" : "volcano"}>
                {record.unrealizedProfitLoss !== null ? Number(record.unrealizedProfitLoss).toLocaleString() : "-"} {record.currency}
              </Tag>
            ),
          },
          {
            title: "Realized P/L",
            render: (_, record: StockPosition) => (
              <Tag color={Number(record.realizedProfitLoss) >= 0 ? "green" : "red"}>
                {Number(record.realizedProfitLoss).toLocaleString()} {record.currency}
              </Tag>
            ),
          },
          { title: "Quote", render: (_, record: StockPosition) => record.quoteSource ? `${record.quoteSource} / ${new Date(record.quoteEffectiveAt ?? record.updatedAt).toLocaleString()}` : "-" },
        ]}
      />
    </Card>
  );
}
