import { Button, Card, Input, Select, Space, Tag, Typography } from "antd";
import type { Dispatch, SetStateAction } from "react";
import type { PageResponse, Transaction } from "../api";
import { OperationsGridTable } from "../components/OperationsGridTable";

type TransactionFilter = {
  query: string;
  status?: string;
  transactionType?: string;
  minAmount?: string;
  maxAmount?: string;
  occurredFrom?: string;
  occurredTo?: string;
  sortBy: string;
  sortDir: string;
  page: number;
  size: number;
};

type TransactionsPageProps = {
  data: PageResponse<Transaction>;
  filter: TransactionFilter;
  setFilter: Dispatch<SetStateAction<TransactionFilter>>;
  onSearch: () => void;
  onReset: () => void;
  onPageChange: (page: number, size: number) => void;
};

export function TransactionsPage({ data, filter, setFilter, onSearch, onReset, onPageChange }: TransactionsPageProps) {
  return (
    <Card
      className="ops-panel-card"
      title={
        <div>
          <p className="eyebrow">Ledger Review</p>
          <Typography.Title level={3} style={{ margin: 0 }}>Transactions</Typography.Title>
        </div>
      }
      extra={<Typography.Text type="secondary">총 {data.totalElements}건</Typography.Text>}
    >
      <Space wrap className="filter-row ops-filter-row">
        <Input
          placeholder="거래번호 / 통화 검색"
          value={filter.query}
          onChange={(event) => setFilter((current) => ({ ...current, query: event.target.value, page: 0 }))}
          onPressEnter={onSearch}
        />
        <Select
          allowClear
          placeholder="거래 상태"
          style={{ width: 160 }}
          value={filter.status}
          onChange={(value) => setFilter((current) => ({ ...current, status: value, page: 0 }))}
          options={[
            { label: "COMPLETED", value: "COMPLETED" },
            { label: "PENDING", value: "PENDING" },
            { label: "REJECTED", value: "REJECTED" },
          ]}
        />
        <Select
          allowClear
          placeholder="거래 유형"
          style={{ width: 160 }}
          value={filter.transactionType}
          onChange={(value) => setFilter((current) => ({ ...current, transactionType: value, page: 0 }))}
          options={[
            { label: "DEPOSIT", value: "DEPOSIT" },
            { label: "WITHDRAWAL", value: "WITHDRAWAL" },
            { label: "BUY", value: "BUY" },
            { label: "SELL", value: "SELL" },
          ]}
        />
        <Input
          type="number"
          placeholder="최소 금액"
          value={filter.minAmount}
          onChange={(event) => setFilter((current) => ({ ...current, minAmount: event.target.value, page: 0 }))}
          style={{ width: 160 }}
        />
        <Input
          type="number"
          placeholder="최대 금액"
          value={filter.maxAmount}
          onChange={(event) => setFilter((current) => ({ ...current, maxAmount: event.target.value, page: 0 }))}
          style={{ width: 160 }}
        />
        <Input
          type="date"
          placeholder="시작일"
          value={filter.occurredFrom}
          onChange={(event) => setFilter((current) => ({ ...current, occurredFrom: event.target.value, page: 0 }))}
          style={{ width: 160 }}
        />
        <Input
          type="date"
          placeholder="종료일"
          value={filter.occurredTo}
          onChange={(event) => setFilter((current) => ({ ...current, occurredTo: event.target.value, page: 0 }))}
          style={{ width: 160 }}
        />
        <Select
          placeholder="정렬 기준"
          style={{ width: 160 }}
          value={filter.sortBy}
          onChange={(value) => setFilter((current) => ({ ...current, sortBy: value, page: 0 }))}
          options={[
            { label: "거래 시각", value: "occurredAt" },
            { label: "거래번호", value: "transactionNumber" },
            { label: "금액", value: "amount" },
          ]}
        />
        <Select
          placeholder="정렬 방향"
          style={{ width: 140 }}
          value={filter.sortDir}
          onChange={(value) => setFilter((current) => ({ ...current, sortDir: value, page: 0 }))}
          options={[
            { label: "내림차순", value: "desc" },
            { label: "오름차순", value: "asc" },
          ]}
        />
        <Button type="primary" onClick={onSearch}>Search</Button>
        <Button onClick={onReset}>Reset</Button>
      </Space>
      <OperationsGridTable
        rowKey="id"
        dataSource={data.items}
        pagination={{
          current: data.page + 1,
          pageSize: data.size,
          total: data.totalElements,
          onChange: (page, size) => onPageChange(page, size),
        }}
        columns={[
          { title: "Transaction No", dataIndex: "transactionNumber" },
          { title: "Type", dataIndex: "transactionType" },
          { title: "Amount", render: (_, record: Transaction) => `${Number(record.amount).toLocaleString()} ${record.currency}` },
          { title: "Status", dataIndex: "status", render: (value: string) => <Tag color="gold">{value}</Tag> },
          { title: "Description", dataIndex: "description" },
          { title: "Occurred At", render: (_, record: Transaction) => new Date(record.occurredAt).toLocaleString() },
        ]}
      />
    </Card>
  );
}
