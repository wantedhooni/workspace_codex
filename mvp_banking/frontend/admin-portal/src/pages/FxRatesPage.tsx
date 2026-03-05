import { Card } from "antd";
import type { FxRate } from "../api";
import { OperationsGridTable } from "../components/OperationsGridTable";

type FxRatesPageProps = {
  rates: FxRate[];
};

export function FxRatesPage({ rates }: FxRatesPageProps) {
  return (
    <Card title="FX Rates">
      <OperationsGridTable
        rowKey="id"
        dataSource={rates}
        pagination={false}
        columns={[
          { title: "Base", dataIndex: "baseCurrency" },
          { title: "Quote", dataIndex: "quoteCurrency" },
          { title: "Rate", render: (_, record: FxRate) => Number(record.rate).toLocaleString() },
          { title: "Source", dataIndex: "source" },
          { title: "Effective At", render: (_, record: FxRate) => new Date(record.effectiveAt).toLocaleString() },
        ]}
      />
    </Card>
  );
}
