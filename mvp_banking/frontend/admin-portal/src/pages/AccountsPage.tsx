import { Button, Card, Input, Select, Space, Tag, Typography } from "antd";
import type { Dispatch, SetStateAction } from "react";
import type { Account, PageResponse } from "../api";
import { OperationsGridTable } from "../components/OperationsGridTable";

type AccountFilter = {
  query: string;
  status?: string;
  accountType?: string;
  minBalance?: string;
  maxBalance?: string;
  sortBy: string;
  sortDir: string;
  page: number;
  size: number;
};

type AccountsPageProps = {
  data: PageResponse<Account>;
  filter: AccountFilter;
  setFilter: Dispatch<SetStateAction<AccountFilter>>;
  onSearch: () => void;
  onReset: () => void;
  onPageChange: (page: number, size: number) => void;
};

export function AccountsPage({ data, filter, setFilter, onSearch, onReset, onPageChange }: AccountsPageProps) {
  return (
    <Card
      className="ops-panel-card"
      title={
        <div>
          <p className="eyebrow">Account Registry</p>
          <Typography.Title level={3} style={{ margin: 0 }}>Accounts</Typography.Title>
        </div>
      }
      extra={<Typography.Text type="secondary">총 {data.totalElements}건</Typography.Text>}
    >
      <Space wrap className="filter-row ops-filter-row">
        <Input
          placeholder="계좌번호 / 통화 검색"
          value={filter.query}
          onChange={(event) => setFilter((current) => ({ ...current, query: event.target.value, page: 0 }))}
          onPressEnter={onSearch}
        />
        <Select
          allowClear
          placeholder="계좌 상태"
          style={{ width: 160 }}
          value={filter.status}
          onChange={(value) => setFilter((current) => ({ ...current, status: value, page: 0 }))}
          options={[
            { label: "ACTIVE", value: "ACTIVE" },
            { label: "LOCKED", value: "LOCKED" },
            { label: "PENDING_APPROVAL", value: "PENDING_APPROVAL" },
          ]}
        />
        <Select
          allowClear
          placeholder="계좌 유형"
          style={{ width: 160 }}
          value={filter.accountType}
          onChange={(value) => setFilter((current) => ({ ...current, accountType: value, page: 0 }))}
          options={[
            { label: "BANKING", value: "BANKING" },
            { label: "SECURITIES", value: "SECURITIES" },
          ]}
        />
        <Input
          type="number"
          placeholder="최소 잔액"
          value={filter.minBalance}
          onChange={(event) => setFilter((current) => ({ ...current, minBalance: event.target.value, page: 0 }))}
          style={{ width: 160 }}
        />
        <Input
          type="number"
          placeholder="최대 잔액"
          value={filter.maxBalance}
          onChange={(event) => setFilter((current) => ({ ...current, maxBalance: event.target.value, page: 0 }))}
          style={{ width: 160 }}
        />
        <Select
          placeholder="정렬 기준"
          style={{ width: 160 }}
          value={filter.sortBy}
          onChange={(value) => setFilter((current) => ({ ...current, sortBy: value, page: 0 }))}
          options={[
            { label: "최근 생성순", value: "createdAt" },
            { label: "계좌번호", value: "accountNumber" },
            { label: "잔액", value: "balance" },
            { label: "통화", value: "currency" },
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
          { title: "Account No", dataIndex: "accountNumber" },
          { title: "Type", dataIndex: "accountType" },
          { title: "Balance", render: (_, record: Account) => `${Number(record.balance).toLocaleString()} ${record.currency}` },
          { title: "Status", dataIndex: "status", render: (value: string) => <Tag color="cyan">{value}</Tag> },
        ]}
      />
    </Card>
  );
}
