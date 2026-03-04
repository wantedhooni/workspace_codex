import { Card, Table, Tag } from "antd";
import type { ExchangeRequest } from "../api";

type ExchangeRequestsPageProps = {
  exchangeRequests: ExchangeRequest[];
};

export function ExchangeRequestsPage({ exchangeRequests }: ExchangeRequestsPageProps) {
  return (
    <Card title="Exchange Requests">
      <Table
        rowKey="id"
        dataSource={exchangeRequests}
        pagination={false}
        columns={[
          { title: "Request No", dataIndex: "requestNumber" },
          {
            title: "Flow",
            render: (_, record: ExchangeRequest) => (
              <div>
                <div>{`${shortId(record.sourceAccountId)} ${record.fromCurrency} -> ${shortId(record.destinationAccountId)} ${record.toCurrency}`}</div>
                <small>{`${record.fromCurrency}/${record.toCurrency}`}</small>
              </div>
            ),
          },
          { title: "From", render: (_, record: ExchangeRequest) => `${Number(record.fromAmount).toLocaleString()} ${record.fromCurrency}` },
          { title: "Rate", render: (_, record: ExchangeRequest) => Number(record.appliedRate).toLocaleString() },
          {
            title: "Settlement",
            render: (_, record: ExchangeRequest) => (
              <div>
                <div>{`gross ${Number(record.toAmount).toLocaleString()} ${record.toCurrency}`}</div>
                <small>{`fee ${Number(record.exchangeFeeAmount).toLocaleString()} / net ${Number(record.netToAmount).toLocaleString()} ${record.toCurrency}`}</small>
              </div>
            ),
          },
          { title: "Status", dataIndex: "status", render: (value: string) => <Tag color={value === "APPROVED" ? "green" : value === "REJECTED" ? "red" : "gold"}>{value}</Tag> },
          {
            title: "Settlement Legs",
            render: (_, record: ExchangeRequest) => (
              <div>
                <div>{`OUT ${record.sourceTransactionNumber ?? "-"}`}</div>
                <div>{`IN ${record.destinationTransactionNumber ?? "-"}`}</div>
              </div>
            ),
          },
          { title: "Settled At", render: (_, record: ExchangeRequest) => record.settledAt ? new Date(record.settledAt).toLocaleString() : "-" },
          { title: "Created At", render: (_, record: ExchangeRequest) => new Date(record.createdAt).toLocaleString() },
        ]}
      />
    </Card>
  );
}

function shortId(value: string | null) {
  return value ? value.slice(0, 8) : "legacy";
}
